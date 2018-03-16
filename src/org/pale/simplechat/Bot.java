package org.pale.simplechat;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.Function;
import org.pale.simplechat.actions.InstructionCompiler;
import org.pale.simplechat.actions.InstructionStream;

/**
 * encapsulates a bot - a style of conversation loaded from a file.
 * Individual entities which can be conversed with are BotInstance objects.
 * @author white
 *
 */

public class Bot {
	// Things in bot may also be used. It's set by using the "inherit" command.
	public Bot parent=null;  
	private Map<String,Topic> topicsByName;
	List<SubstitutionsInterface> subs; 
	public InstructionStream initAction; // an Action to initialise bot variables etc.
	
	// we insert this into the subs list when we we want to process parent
	// substitutions
	public class SubstitutionsFromParent implements SubstitutionsInterface {
		@Override
		public String process(String s) {
			if(parent!=null)
				return parent.processSubs(s);
			else
				return s;
		}
	}
	

	// this is a list of topic lists.
	// When matching, we run through each topic list in turn.
	// Within a topic list, we run through each topic in turn trying to match its patterns.
	// The reason for the two levels is so that we can "promote" and "demote" topics, but
	// only within their containing lists - the first list always runs first, before any topic in the second
	// list, although we can promote a topic to the top of the second list.
	// All topics exist in the same namespace, however.
	// This list, and sublists, gets cloned into each conversation so that the promoted/demoted topics
	// are per-conversation.
	List<List<Topic>> topicLists;

	/// map of user functions we might use,
	
	private Map<String, Function> funcMap;
	public Function getFunc(String name){
		Bot b = this;
		do {
			if(b.funcMap.containsKey(name))return b.funcMap.get(name);
			b = b.parent;
		} while(b!=null);
		return null;
	}
	public void putFunc(String n, Function f) {
		funcMap.put(n,f);
	}


	// reload all the bot's data!
	public void reload() throws BotConfigException {
		topicLists = new ArrayList<List<Topic>>();
		subs = new ArrayList<SubstitutionsInterface>();
		funcMap =  new HashMap<String,Function>();
		topicsByName = new HashMap<String,Topic>();
		parent = null;
		
		parseConfig(path);

		// read the configuration data
		
		for(Entry<String, Category> f: cats.entrySet()){
			Logger.log(Logger.CONFIG,"Category "+f.getKey()+":");
			f.getValue().dump();
		}
	}
	
	// a map of all the loaded bots.
	private static Map<Path,Bot> loadedBots = new HashMap<Path,Bot>();

	private String name;
	public String getName(){return name;}


	private Path path; // the path of the bot's data, stored for reload
	private Map<String,Category> cats = new HashMap<String,Category>(); // bot's private categories
	
	// this runs up the hierarchy of bots scanning the categories that bot knows about.
	public Category getCategory(String name){
		Bot b = this;
		do {
			if(b.cats.containsKey(name))return b.cats.get(name);
			b = b.parent;
		} while(b!=null);
			
		return null;	
	}
	public void addCategory(String name,Category c){
		cats.put(name,c);
	}

	// parse a config.conf in the directory Path
	private void parseConfig(Path p) throws BotConfigException{
		try {
			StreamTokenizer tok = new Tokenizer(p.resolve("config.conf"));
			try {
				for(;;) {
					int t = tok.nextToken();
					if(t == StreamTokenizer.TT_EOF)break;
					else if(t == ':') {
						try {
							InstructionCompiler.parseNamedFunction(this,tok);
						} catch (ParserError e){
							throw new BotConfigException(p,tok,"error in a config file function: "+e.getMessage());
						}
					} else if(t == StreamTokenizer.TT_WORD){
						if(tok.sval.equals("inherit")){
							if(tok.nextToken()!='\"')
								throw new BotConfigException(p,tok,"need a \"quoted\" string after inherit");
							inherit(tok.sval);
						} else if(tok.sval.equals("topics")){
							List<Topic> list = parseTopicList(p,tok);
							topicLists.add(list);
						} else if(tok.sval.equals("init")){
							initAction = new InstructionStream(this,tok);
						} else if(tok.sval.equals("subs")){
							switch(tok.nextToken()){
							case StreamTokenizer.TT_WORD:
								if(tok.sval.equals("parent"))
									subs.add(new SubstitutionsFromParent());
								else
									throw new BotConfigException(p,tok,"'subs' should be followed by 'parent' or a subs file name in \"quotes\"");
								break;
							case '"':
								subs.add(new Substitutions(p,tok.sval));
								break;
							default:
								throw new BotConfigException(p,tok,"'subs' should be followed by 'parent' or a subs file name in \"quotes\"");
							}
						} else throw new BotConfigException(p,tok,"unknown word in config: "+tok.sval);
					}
				}
			} catch (ParserError e){
				throw new BotConfigException(p,tok,"error in init action : "+e.getMessage());

			}
		} catch (IOException e) {
			throw new BotConfigException("cannot open config file: "+p.toAbsolutePath().resolve("config.conf").toString());
		}
	}

	private void inherit(String sval) throws BotConfigException {
		// attempt to load the specified bot
		Path p = Paths.get(sval);
		parent = new Bot(p);
		
	}

	private List<Topic> parseTopicList(Path p, StreamTokenizer tok) throws IOException, BotConfigException {
		List<Topic> topicList = new ArrayList<Topic>();
		if(tok.nextToken() != '{')throw new BotConfigException(p,tok,"expected '{' after 'topics'");
		for(;;){
			if(tok.nextToken() == '}')break;
			else tok.pushBack();

			if(tok.nextToken() == StreamTokenizer.TT_WORD){
				String name = tok.sval;
				Topic t = getTopic(name); // does the topic exist?
				if(t==null){
					// if not, load and add it to our topic map
					t = new Topic(this,name,p.resolve(name+".topic"));
					topicsByName.put(name, t);
				}
				// add the topic to run here in the list.
				topicList.add(t);
			}
		}
		return topicList;
	}

	public Topic getTopic(String name){
		// we run up the hierarchy to get this topic
		Bot b = this;
		do {
			if(b.topicsByName.containsKey(name))
				return b.topicsByName.get(name);
			b = b.parent;
		} while(b!=null);
		return null;
	}

	public Bot(Path path) throws BotConfigException{
		this.path = path; // store this so we can reload
		name = path.getFileName().toString();
		reload();
		
		// store the loaded bot as an absolute path
		loadedBots.put(path.toAbsolutePath(), this);

		Logger.log(Logger.LOAD,name+" : created bot OK");
	}
	
	/// process substitutions on a string
	public String processSubs(String s) {
		for(SubstitutionsInterface sub: subs){
			s = sub.process(s);
		}
		return s;
	}
	
	// run the init function on an instance, through a conversation, running all the parents first.
	// Recurses.
	public void runInits(Conversation c) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, ActionException{
		if(parent!=null)
			parent.runInits(c);
		initAction.run(c, true);
	}


}
