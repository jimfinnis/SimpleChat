package org.pale.simplechat;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.MalformedInputException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

//import javax.xml.bind.JAXBElement.GlobalScope;

import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.Function;
import org.pale.simplechat.actions.InstructionCompiler;
import org.pale.simplechat.actions.InstructionStream;
import org.pale.simplechat.actions.Value;
import org.pale.simplechat.values.NoneValue;

/**
 * encapsulates a bot - a style of conversation loaded from a file.
 * Individual entities which can be conversed with are BotInstance objects.
 * @author white
 *
 */

public class Bot {

	/**
	 * You must define one of these to tell the system where to load the bots from.
	 * It takes a bot name and returns a path, preferably absolute.
	 * @author white
	 *
	 */
	public interface PathProvider {
		public Path path(String name);
	}
	private static PathProvider pathProvider=null; // the path provider, which must be set!
	// list of all the bots, used in "inherit".
	static Map<String,Bot> bots = new HashMap<String,Bot>();

	private String name;
	private Path path; // the path of the bot's data, stored for reload

	// Things in bot may also be used. It's set by using the "inherit" command.
	public Bot parent=null;  
	private Map<String,Topic> topicsByName;
	private Map<String,Pattern> storedPats = new HashMap<String,Pattern>();
	private Map<String,Category> cats = new HashMap<String,Category>(); // bot's private categories
	private Map<String,PhraseList> phraseLists = new HashMap<String,PhraseList>();
	List<SubstitutionsInterface> subs;
	// list of all my instances
	Set<BotInstance> instances = new HashSet<BotInstance>();

	public InstructionStream initAction=null; // an Action to initialise INSTANCE variables etc.
	public InstructionStream globalAction=null; // an Action to initialise GLOBAL variables AND THAT'S ALL.

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
	
	/// this is a "dummy" bot instance used to store global variable data.
	/// How it works: the globals action is run with this instance, so instance variables are stored here.
	/// If a search for an instance variable in getVar fails, it falls back to looking in here. Thus, only one
	/// copy of constant data is kept. If this variable is set, it will of course create a private copy which
	/// will be in the instance and override the global data (and not change it for any other instance).
	BotInstance globalInstance = null;

	

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

	public String getName(){return name;}

	public Function getFunc(String name){
		Bot b = this;
		do {
			if(b.funcMap.containsKey(name))return b.funcMap.get(name);
			b = b.parent;
		} while(b!=null);
		return null;
	}

	public boolean hasFunc(String name){
		return getFunc(name)!=null;
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
		cats = new HashMap<String,Category>();
		parent = null;

		parseConfig(path,"config.conf");
		
		for(Entry<String, Category> e: cats.entrySet()){
			if(e.getValue().containsRecurse(e.getValue()))
				throw new BotConfigException("Category contains recursion: "+e.getKey());
		}

/*
		for(Entry<String, Category> f: cats.entrySet()){
			Logger.log(Logger.CONFIG,"Category "+f.getKey()+":");
			f.getValue().dump();
		}
*/	}

	// this runs up the hierarchy of bots scanning the categories that bot knows about.
	public Category getCategory(String name, boolean mustExist){
		Bot b = this;
		do {
			if(b.cats.containsKey(name))return b.cats.get(name);
			b = b.parent;
		} while(b!=null);

		if(mustExist)
			return null;
		else {
			Category blankCat = new Category(name);
			cats.put(name, blankCat);
			return blankCat;
		}
			
	}

	// this runs up the hierarchy of bots scanning the lists that bot knows about.
	public PhraseList getPhraseList(String name) {
		Bot b = this;
		do {
			if(b.phraseLists.containsKey(name)) {
				return b.phraseLists.get(name);
			}
			b = b.parent;
		} while(b!=null);
		return null;	
	}
	
	public Pattern getStoredPattern(String name){
		Bot b = this;
		do {
			if(b.storedPats.containsKey(name))return b.storedPats.get(name);
			b = b.parent;
		} while(b!=null);
		return null;
	}
	
	public Value getGlobalVar(String s) {
		Bot b = this;
		do {
			if(b.globalInstance!=null && b.globalInstance.vars.containsKey(s))
				return b.globalInstance.vars.get(s);
			b = b.parent;
		} while(b!=null);
		return NoneValue.instance;
	}





	// parse a config.conf in the directory Path
	private void parseConfig(Path p,String filename) throws BotConfigException{
		String fn = p.resolve(filename).toAbsolutePath().toString();
		try {
			Tokenizer tok = new Tokenizer(p.resolve(filename));

			try {
				for(;;) {
					int t = tok.nextToken();
					if(t == StreamTokenizer.TT_EOF)break;
					else if(t == ':') {
						try {
							InstructionCompiler.parseNamedFunction(this,tok);
						} catch (ParserError e){
							throw new BotConfigException(p,tok,"error in a config file function: "+e.toString());
						}
					} else if(t == '~'){
						Category.parseCat(this, tok);

					} else if(t == '^'){
						if(tok.nextToken()!=StreamTokenizer.TT_WORD)
							throw new ParserError("expected name in list");
						String name = tok.sval;
						if(tok.nextToken()!='=')
							throw new ParserError("expected =[ after list name");
						if(tok.nextToken()!='[')
							throw new ParserError("expected =[ after list name");
						phraseLists.put(name,new PhraseList(tok));
						Logger.log(Logger.CONFIG,"^^^ new phrase list: "+name);
					} else if(t == '&'){
						if(tok.nextToken()!=StreamTokenizer.TT_WORD)
							throw new ParserError("expected name in stored pattern");
						String name = tok.sval;
						if(tok.nextToken()!='=')
							throw new ParserError("expected = after pattern name");
						storedPats.put(name,new Pattern(this,null,tok));
						Logger.log(Logger.CONFIG,"^^^ new phrase list: "+name);
					} else if(t == StreamTokenizer.TT_WORD){
						if(tok.sval.equals("inherit")){
							if(tok.nextToken()!=StreamTokenizer.TT_WORD)
								throw new BotConfigException(p,tok,"need an unquoted bot name after inherit");
							inherit(tok.sval);
						} else if(tok.sval.equals("include")){
							if(tok.nextToken()!='"')
								throw new BotConfigException(p,tok,"need an quoted filename after include");
							parseConfig(p,tok.sval);
						} else if(tok.sval.equals("topics")){
							switch(tok.nextToken()){
							case '{':
								if(parent!=null && topicLists == parent.topicLists)
									throw new BotConfigException("cannot add a topic list when we've inherited them from the parent");
								List<Topic> list = parseTopicList(p,tok);
								topicLists.add(list);
								break;
							case StreamTokenizer.TT_WORD:
								if(tok.sval.equals("inherit")){
									if(parent == null)
										throw new BotConfigException("cannot inherit parent topics when we don't have a parent");
									// we just inherit the parent topics - and make sure we haven't got any yet
									// and that we don't add any more!
									if(topicLists.isEmpty())
										topicLists = parent.topicLists;
									else
										throw new BotConfigException("cannot inherit topic lists when we have created some here already");
									break; // end case
								}
								// fall through
							default:
								throw new BotConfigException(p,tok,"expected '{' or 'inherit' after 'topics'");
							}
						} else if(tok.sval.equals("init")){
							initAction = new InstructionStream(this,tok);
						} else if(tok.sval.equals("global")){
							globalAction = new InstructionStream(this,tok);
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
						} else if(tok.sval.equals("skipif")){
							if(parseCondition(tok)){
								skipUntilEndSkip(tok);
							}
						} else if(tok.sval.equals("endskip")){ // does nowt
						} else if(tok.sval.equals("message")){
							if(tok.nextToken()!='"')
								throw new BotConfigException("expected a string after message");
							Logger.log(Logger.ALWAYS,"MESSAGE: "+tok.sval);
						} else if(tok.sval.equals("abort")){
							if(tok.nextToken()!='"')
								throw new BotConfigException("expected a string after abort");
							Logger.log(Logger.ALWAYS,"ABORT: "+tok.sval);
							throw new BotConfigException("ABORTED WITH "+tok.sval);
						}
						else throw new BotConfigException(p,tok,"unknown word in config: "+tok.sval);
					}
				}
				
				// we now create a "fake" instance just for storing global data (ONLY if we have some)
				if(globalAction != null){
					globalInstance = new BotInstance(this, "globals");
					Conversation conv = new Conversation(globalInstance,globalInstance);
					try {
						globalAction.run(conv, true);
					} catch (IllegalAccessException | IllegalArgumentException
							| InvocationTargetException | ActionException e) {
						e.printStackTrace();
						throw new BotConfigException("Error in global action "+e.toString());
					}
				}
				
				
				
			} catch (ParserError e){
				throw new BotConfigException(p,tok,"error in config "+fn+": "+e.toString());

			}
		} catch (MalformedInputException e){
			throw new BotConfigException("Config file "+fn+" is malformed (bad charset?)");
		} catch (IOException e) {
			throw new BotConfigException("cannot open config file: "+fn);
		}
	}

	private void skipUntilEndSkip(StreamTokenizer tok) throws IOException {
		for(;;){
			int t = tok.nextToken();
			if(t == StreamTokenizer.TT_EOF)break;
			if(t == StreamTokenizer.TT_WORD && tok.sval.equals("endskip"))break;
		}
	}

	private boolean parseCondition(StreamTokenizer tok) throws IOException, BotConfigException {
		boolean negate,cond;
		if(tok.nextToken()=='!')
			negate = true;
		else {
			negate=false;
			tok.pushBack();
		}
		if(tok.nextToken()!=StreamTokenizer.TT_WORD)
			throw new BotConfigException("Expected a word in skip condition");
		if(tok.sval.equals("extension")){
			if(tok.nextToken()!=StreamTokenizer.TT_WORD)
				throw new BotConfigException("Expected an extension name in skip condition");
			cond = InstructionCompiler.hasExtension(tok.sval);
		} else
			throw new BotConfigException("Unknown skip condition");

		if(negate)cond=!cond;
		return cond;
	}


	private void inherit(String sval) throws BotConfigException {
		Logger.log(Logger.CONFIG, " Bot "+name+" inheriting "+sval);
		if(sval.equals(name))
			throw new BotConfigException("Bots cannot inherit themselves.");

		parent = loadBot(sval);		
	}

	private List<Topic> parseTopicList(Path p, StreamTokenizer tok) throws IOException, BotConfigException {
		List<Topic> topicList = new ArrayList<Topic>();
		
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

	/**
	 * Use this to set the thingy which turns bot names into paths - i.e. which tells the 
	 * system where the bots live in the filesystem.
	 * @param p
	 */
	public static void setPathProvider(PathProvider p){
		pathProvider = p;
	}

	/**
	 * Use this to load a bot
	 * @param n name of the bot
	 * @return the bot, either pre-existing or a completely new one whose path name is found with the path provider.
	 * @throws BotConfigException 
	 */
	public static Bot loadBot(String n) throws BotConfigException{
		Bot b;

		Logger.log(Logger.LOAD, "");
		Logger.log(Logger.LOAD, "##############################################################################################");
		Logger.log(Logger.LOAD, "#### ATTEMPTING TO LOAD BOT: "+n);
		Logger.log(Logger.LOAD, "##############################################################################################");
		Logger.log(Logger.LOAD, "");
		
		if(bots.containsKey(n)){
			b = bots.get(n);
		} else {
			if(pathProvider == null)
				throw new BotConfigException("The path provider has not been set!");
			Path p = pathProvider.path(n);
			if(p==null)
				throw new BotConfigException("The path provider returns null for "+n+"!");
			else {
				Logger.log(Logger.CONFIG, "Name provided for "+n+" is "+p.toAbsolutePath());
				b = new Bot(n,p);
			}
		}
		Logger.log(Logger.LOAD, "");
		Logger.log(Logger.LOAD, "##############################################################################################");
		Logger.log(Logger.LOAD, "#### SUCCESSFULLY LOADED BOT: "+n);
		Logger.log(Logger.LOAD, "##############################################################################################");
		Logger.log(Logger.LOAD, "");
		return b;
	}

	/**
	 * Bot constructor - please use loadBot instead!
	 * @param name   name of the bot, used when we inherit (inherited bots must already be loaded)
	 * @param path   the bot's path
	 * @throws BotConfigException
	 */
	private Bot(String name, Path path) throws BotConfigException{
		this.path = path; // store this so we can reload
		this.name = name;
		reload();

		// store the loaded bot under its name
		bots.put(name, this);

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
		if(initAction!=null)
			initAction.run(c, true);
	}


}
