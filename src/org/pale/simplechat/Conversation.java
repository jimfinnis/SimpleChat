package org.pale.simplechat;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.ActionLog;
import org.pale.simplechat.actions.Function;
import org.pale.simplechat.actions.Runtime;
import org.pale.simplechat.actions.Value;
import org.pale.simplechat.patterns.MatchData;
import org.pale.simplechat.values.NoneValue;
import org.pale.simplechat.values.StringValue;

/**
 * This contains data about the relationship between a bot instance and another person, i.e.
 * about a single conversation. A conversation also acts as the "runtime" for actions, incorporating
 * its stack and variables.
 * @author white
 *
 */
public class Conversation extends Runtime {
	public BotInstance instance;
	public Object source;
	public boolean debug=false;

	/// variables private to this conversation
	private Map<String,Value> vars = new TreeMap<String,Value>();
	/// variables local to any function, none by default
	public Map<String,Value> funcVars = null; 
	
	/// an output buffer
	StringBuilder output=null;
	
	/// which topic I'm processing, null if we're doing a special
	private Topic curTopic;
	public Topic getCurTopic(){
		return curTopic;
	}

	// gets a function local if it exists, failing that a conversation local.
	public Value getVar(String s){
		if(funcVars!=null && funcVars.containsKey(s))
			return funcVars.get(s);
		else if(vars.containsKey(s))
			return vars.get(s);
		else
			return NoneValue.instance;
	}

	// sets a function local if it exists, failing that a conversation local.
	public void setVar(String name, Value v) {
		if(funcVars!=null && funcVars.containsKey(name))
			funcVars.put(name,v);
		else
			vars.put(name,v);
	}


	/// variables which came out of the last pattern match
	private Map<String,String> patvars;
	public Value getPatVar(String s){
		if(patvars.containsKey(s))
			return new StringValue(patvars.get(s));
		else
			return NoneValue.instance;
	}

	// used for creating "dummy" conversations for constant blocks 
	public Conversation(){
		source = null;
		instance = null;
		topicLists = new ArrayList<Deque<Topic>>();
	}
	
	// the usual ctor for creating real conversations (and also the init conversation with myself)
	Conversation(BotInstance i,Object p){
		source = p;
		instance = i;
		cloneTopicLists();
	}

	// our private copy of the bot topic lists, so we can rearrange them for this conversation only. 
	List<Deque<Topic>> topicLists;

	// we need to clone the topic lists, because we need to be free to promote and demote topics.
	// Note that in the clones, we actually create deques because of the need to promote/demote.
	void cloneTopicLists(){
		topicLists = new ArrayList<Deque<Topic>>();
		for(List<Topic> list: instance.bot.topicLists){
			Deque<Topic> newlist = new LinkedList<Topic>();
			topicLists.add(newlist);
			for(Topic t: list)
				newlist.add(t);
		}
	}

	/// if not null, the action we ran before has set explicit patterns
	//// perhaps a dialog tree in subpatterns.
	public List<Pair> specialpats = null;
	/// these are held subpatterns - although the action system wipes subpatterns before an
	/// action runs, these remember what they were so they can be restored if we run "holdnext".
	private List<Pair> heldSubPats;
	
	// called from the "holdnext" command.
	public void holdnext(){
		specialpats = heldSubPats;
	}

	/// try to handle a pattern/action pair by matching the pattern and if a match, running the action.
	/// If no match return null.
	public String handle(String s,Pair p){
		output = new StringBuilder(); // clear the output string.
		MatchData m = p.pat.match(s);
		if(!m.invalid){
			// output the labelled match results to the log
			for(Entry<String, String> x: m.labels.entrySet()){
				Logger.log(Logger.PATTERN,"label "+x.getKey()+"="+x.getValue());
			}
			patvars = m.labels; // set them so we can access them in actions
			try {
				// clear subpatterns here. We might set them in the resulting action,
				// but if we don't we should default to the top level search.
				// However, we store them in heldSubPats, so that if the "holdnext" command runs
				// we can restore them. This is so that catchall patterns can not clear the subpatterns.
				heldSubPats = specialpats;
				specialpats = null; 
				p.act.run(this,true);
				// at this point there should be something on the stack or in the SB, and subpatterns
				// may have been set. Note that this can return null if neither are true, in which case 
				// we'll try the next pattern!
				return getResult();
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | ActionException e) {
				e.printStackTrace();
				Logger.log(Logger.FATAL,"Error in run: "+e.getClass().getSimpleName()+","+e.getMessage());
				ActionLog.show();
				return("ERROR in action");
			}
		}
		else
			return null;
	}
	
	public String runFunc(String s) throws ActionException{
//		specialpats=null;    USED to clear this, but it makes RANDSAY and timer functions interfere with the flow of chat.
		Function f = instance.bot.getFunc(s);
		if(f==null)
			throw new ActionException("bot does not define function "+s);
		try {
			output = new StringBuilder(); // clear the output string.
			f.run(this);
			return getResult();
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new ActionException("error in running function "+s+": "+e.getMessage());
		}
	}
	
	// return the result left on the stack. It will be converted to a string,
	// unless it is none, in which case null will be returned.
	// If there is no value on the stack, the output from the string builder used by "."
	// will be returned.
	public String getResult() throws ActionException{
			if(stack.empty())
				return getOutput();
			
			Value v = pop(); // the exception here shouldn't occur.
			if(v.equals(NoneValue.instance))return null;
			String s = v.str();
			if(!stack.empty()){
				Logger.log(Logger.ACTION,"oops - stuff still on the stack (depth is "+stack.size()+")");
			}
			return s;
	}


	// This is a list giving which topics to promote/demote based on the last actions run.
	// It's a list, not a set - and it's a single list of structures with a flag rather than than
	// two lists. This is because the order of the operations must be preserved.
	class PromoteDemote {
		Topic t;
		boolean demote;
		PromoteDemote(Topic t,boolean demote){
			this.t = t;
			this.demote=demote;
		}
	}
	private List<PromoteDemote> toPromoteDemote = new LinkedList<PromoteDemote>();

	public void promoteDemote(Topic t,boolean demote){
		toPromoteDemote.add(new PromoteDemote(t,demote));
	}

	// this is a set of disabled topics - these will never run. Again, this isn't a flag
	// in Topic because Topics are shared. This only enabled/disables in this conversation.
	private Set<Topic> disabledTopics = new HashSet<Topic>();
	public void enableDisableTopic(Topic t, boolean disable) {
		if(disable && !disabledTopics.contains(t)){
			disabledTopics.add(t);
		} else if(!disable && disabledTopics.contains(t))
			disabledTopics.remove(t);
	}

	// this is a set of disabled pattern/action pairs for this convo
	private Set<Pair> disabledPairs = new HashSet<Pair>();
	public void enableDisablePattern(String topicName, String patName, boolean disable) throws ActionException{
		Topic t = instance.bot.getTopic(topicName);
		if(t==null)
			throw new ActionException("cannot find topic: "+topicName);
		if(!t.pairMap.containsKey(patName))
			throw new ActionException("cannot find named pattern '"+patName+"' in topic '"+topicName+"'");
		Pair p = t.pairMap.get(patName);
		if(disable && !disabledPairs.contains(p))
			disabledPairs.add(p);
		else if(!disable && disabledPairs.contains(p))
			disabledPairs.remove(p);
	}

	public String handle(String s) {
		output = null; // clear the builder
		
		// The previous interaction may have promoted or demoted topics. We need to reorder the lists
		// before we try to process them. Demotions or promotions must run in the order they were performed.

		for(PromoteDemote p: toPromoteDemote){
			// find the topic in a list and then move it. We can't just store the list in the topic,
			// because the topic data is shared among all instances and conversations.
			for(Deque<Topic> list : topicLists){
				if(list.contains(p.t)){
					if(p.demote){
						list.remove(p.t);
						list.addLast(p.t);
					} else {
						list.remove(p.t);
						list.addFirst(p.t);
					}
				}
			}
		}
		// promotes and demotes done, now clear them.
		toPromoteDemote.clear();


		// dialogue tree or special topic
		if(specialpats!=null){
			for(Pair p : specialpats){
				String res = handle(s,p);
				if(res!=null){
					Logger.log(Logger.PATTERN,"done special pseudotopic");
					return res;
				}
			}
		}

		// if that failed, we run through the topics in the topic lists.

		for(Deque<Topic> list : topicLists){
			for(Topic t: list){
				if(!disabledTopics.contains(t)){ // only run enabled topics
					Logger.log(Logger.PATTERN,"Testing topic patterns: "+t.name);
					curTopic = t;
					for(Pair p: t.pairList){
						if(!disabledPairs.contains(p)){
							Logger.log(Logger.PATTERN,"Testing pattern "+p.pat.getName());
							String res = handle(s,p);
							if(res!=null){
								Logger.log(Logger.PATTERN,"done pattern "+p.pat.getName());
								curTopic = null;
								return res;
							}
						}
					}
				}
			}
		}

		curTopic = null;
		return "??"; // match failed for any topic.
	}
	
	public void clearOutput(){
		output = new StringBuilder();
	}

	public void appendOutput(String s) {
		if(output==null)output = new StringBuilder();
		output.append(s);
	}
	
	public String getOutput(){
		if(output == null)
			return null;
		return output.toString();
	}

}
