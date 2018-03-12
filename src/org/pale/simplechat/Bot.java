package org.pale.simplechat;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	private Map<String,Topic> topicsByName;
	Substitutions subs; // substitutions to run before topics
	public InstructionStream initAction; // an Action to initialise bot variables etc.

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
	
	public Map<String, Function> funcMap;
	
	public boolean hasFunc(String name){
		return funcMap.containsKey(name);
	}

	// reload all the bot's data!
	public void reload() throws BotConfigException {
		topicLists = new ArrayList<List<Topic>>();
		subs = new Substitutions();
		funcMap =  new HashMap<String,Function>();
		topicsByName = new HashMap<String,Topic>();

		// read the configuration data
		parseConfig(path);
		
		for(Entry<String, Category> f: cats.entrySet()){
			Logger.log("Category "+f.getKey()+":");
			f.getValue().dump();
		}
	}

	private String name;
	public String getName(){return name;}


	private Path path; // the path of the bot's data, stored for reload
	private  Map<String,Category> cats = new HashMap<String,Category>(); // bot's private categories
	public Category getCategory(String name){
		if(cats.containsKey(name))return cats.get(name);
		if(Category.globalCats.containsKey(name))return Category.globalCats.get(name);
		else return null;	
	}
	public void addCategory(String name,Category c){
		cats.put(name,c);
	}

	// parse a config.conf in the directory Path
	private void parseConfig(Path p) throws BotConfigException{
		try {
			StreamTokenizer tok = new StreamTokenizer(Files.newBufferedReader(p.resolve("config.conf"),
					StandardCharsets.UTF_8));
			tok.commentChar('#');
			tok.ordinaryChar('/');
			tok.ordinaryChar('.');
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
						if(tok.sval.equals("topics")){
							List<Topic> list = parseTopicList(p,tok);
							topicLists.add(list);
						} else if(tok.sval.equals("init")){
							initAction = new InstructionStream(this,tok);
						} else if(tok.sval.equals("subs")){
							if(tok.nextToken()!='"')
								throw new BotConfigException(p,tok,"subs should be followed by a subs file name in \"quotes\"");
							subs.parseFile(p,tok.sval);
						} else throw new BotConfigException(p,tok,"unknown word in config: "+tok.sval);
					}
				}
			} catch (ParserError e){
				throw new BotConfigException(p,tok,"error in init action : "+e.getMessage());

			}
		} catch (IOException e) {
			throw new BotConfigException("cannot open config file config.conf in "+p.getFileName());
		}
	}

	private List<Topic> parseTopicList(Path p, StreamTokenizer tok) throws IOException, BotConfigException {
		List<Topic> topicList = new ArrayList<Topic>();
		if(tok.nextToken() != '{')throw new BotConfigException(p,tok,"expected '{' after 'topics'");
		for(;;){
			if(tok.nextToken() == '}')break;
			else tok.pushBack();

			if(tok.nextToken() == StreamTokenizer.TT_WORD){
				String name = tok.sval;
				Topic t = new Topic(this,name,p.resolve(name+".topic"));
				if(topicsByName.containsKey(name)){
					throw new BotConfigException(p,tok,"topic already exists: "+t.name);
				}
				topicList.add(t);
				topicsByName.put(name, t);
			}
		}
		return topicList;
	}

	public Topic getTopic(String name){
		if(topicsByName.containsKey(name))
			return topicsByName.get(name);
		else
			return null;
	}

	public Bot(Path path) throws BotConfigException{
		this.path = path; // store this so we can reload
		name = path.getFileName().toString();
		reload();

		Logger.log(name+" : created bot OK");
	}

}
