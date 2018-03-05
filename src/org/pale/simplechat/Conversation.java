package org.pale.simplechat;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.pale.simplechat.patterns.MatchData;
import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.Runtime;
import org.pale.simplechat.actions.Value;

/**
 * This contains data about the relationship between a bot instance and another person, i.e.
 * about a single conversation. A conversation also acts as the "runtime" for actions, incorporating
 * its stack and variables.
 * @author white
 *
 */
public class Conversation extends Runtime {
	private Source source;
	private BotInstance instance;
	
	// each topic has a priority, and topics are tried in descending priority order. By
	// default the priority is 1. This is the map of default priority, loaded from the topic
	// files (topics can set their priority). It is copied to each new conversation, which
	// can increase or decrease priorities.
	Map<Topic,Double> topicPriorities = new HashMap<Topic,Double>();
	private ArrayList<Topic> topicsSortedByPriority;

	/// variables private to this conversation
	private Map<String,String> vars = new TreeMap<String,String>();

	public String getVar(String s){
		if(vars.containsKey(s))
			return vars.get(s);
		else
			return "??";
	}
	
	/// variables which came out of the last pattern match
	private Map<String,String> patvars;
	public String getPatVar(String s){
		if(patvars.containsKey(s))
			return patvars.get(s);
		else
			return "??";
		
	}
	
	Conversation(BotInstance i,Source p){
		source = p;
		instance = i;
		topicPriorities = new HashMap<Topic,Double>(instance.bot.topicPriorities); // clone topic weights
		topicsSortedByPriority = new ArrayList<Topic>(topicPriorities.keySet()); // this will get sorted..
		sortTopics();
	}
	
	/**
	 * Change a topic weight and reorder the topics
	 */
	void setTopicWeight(Topic t,double w){
		if(topicPriorities.containsKey(t)){
			topicPriorities.put(t, w);
			sortTopics();
		}
	}
	
	private void sortTopics(){
		Collections.sort(topicsSortedByPriority,new Comparator<Topic>() {
			public int compare(Topic a,Topic b){
				return topicPriorities.get(a).compareTo(topicPriorities.get(b));
			}
		});		
	}
	
	/// if not null, the action we ran before has set explicit patterns
	//// perhaps a dialog tree in subpatterns.
	public List<Pair> specialpats = null;

	/// try to handle a pattern/action pair by matching the pattern and if a match, running the action.
	/// If no match return null.
	public String handle(String s,Pair p){
		MatchData m = p.pat.match(s);
		if(!m.invalid){
			// output the labelled match results to the log
			for(Entry<String, String> x: m.labels.entrySet()){
				Logger.log("label "+x.getKey()+"="+x.getValue());
			}
			patvars = m.labels; // set them so we can access them in actions
			try {
				// clear subpatterns here. We might set them in the resulting action,
				// but if we don't we should default to the top level search.
				specialpats = null; 
				p.act.run(this);
				// at this point there should be something on the stack, and subpatterns
				// may have been set.
				return getResult();
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | ActionException e) {
				// TODO Auto-generated catch block
				Logger.log("Error in run: "+e.getClass().getSimpleName()+","+e.getMessage());
				e.printStackTrace();
				return("ERROR in action");
			}
		}
		else
			return null;
	}
	
	public String handle(String s) {
		/*TODO
		 * - if there is a special "topic", try to match the patterns in that first.
		 * - otherwise, go through the topics in weight order attempting matches on their patterns.
		 */
		
		// dialogue tree or special topic
		if(specialpats!=null){
			for(Pair p : specialpats){
				String res = handle(s,p);
				if(res!=null){
					Logger.log("done special pseudotopic");
					return res;
				}
			}
		}
		
		// last resort - run through topics
		for(Topic t: topicsSortedByPriority){
			Logger.log("Testing topic patterns"+t.name);
			for(Pair p: t.pairList){
				String res = handle(s,p);
				if(res!=null){
					Logger.log("done pattern "+p.pat.getName());
					return res;
				}
			}
		}
		
		return "??";
	}



}
