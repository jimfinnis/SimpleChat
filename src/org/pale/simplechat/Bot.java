package org.pale.simplechat;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pale.simplechat.actions.Action;

/**
 * encapsulates a bot - a style of conversation loaded from a file.
 * Individual entities which can be conversed with are BotInstance objects.
 * @author white
 *
 */

public class Bot {
	private Map<String,Topic> topicsByName = new HashMap<String,Topic>();
	

	Substitutions subs = new Substitutions(); // substitutions to run before topics

	public Action initAction; // an Action to initialise bot variables etc.
	
	// this is a list of topic lists.
	// When matching, we run through each topic list in turn.
	// Within a topic list, we run through each topic in turn trying to match its patterns.
	// The reason for the two levels is so that we can "promote" and "demote" topics, but
	// only within their containing lists - the first list always runs first, before any topic in the second
	// list, although we can promote a topic to the top of the second list.
	// All topics exist in the same namespace, however.
	// This list, and sublists, gets cloned into each conversation so that the promoted/demoted topics
	// are per-conversation.
	List<List<Topic>> topicLists = new ArrayList<List<Topic>>();
	
	// parse a config.conf in the directory Path
	private void parseConfig(Path p) throws BotConfigException{
		try {
			StreamTokenizer tok = new StreamTokenizer(Files.newBufferedReader(p.resolve("config.conf")));
			tok.commentChar('#');
			tok.ordinaryChar('/');
			for(;;) {
				int t = tok.nextToken();
				if(t == StreamTokenizer.TT_EOF)break;
				else if(t == StreamTokenizer.TT_WORD){
					if(tok.sval.equals("topics")){
						List<Topic> list = parseTopicList(p,tok);
						topicLists.add(list);
					} else if(tok.sval.equals("init")){
						initAction = new Action(tok);
					} else if(tok.sval.equals("subs")){
						if(tok.nextToken()!='"')
							throw new BotConfigException("subs should be followed by a subs file name in \"quotes\"");
						subs.parseFile(p,tok.sval+".sub");
					}
				}
			}
		} catch (IOException e) {
			throw new BotConfigException("cannot open config file config.conf in "+p.getFileName());
		} catch (TopicSyntaxException|PatternParseException e) {
			throw new BotConfigException("error in init action : "+e.getMessage());
		}
	}
	
	private List<Topic> parseTopicList(Path p, StreamTokenizer tok) throws IOException, BotConfigException {
		List<Topic> topicList = new ArrayList<Topic>();
		if(tok.nextToken() != '{')throw new BotConfigException("expected '{' after 'topics'");
		for(;;){
			if(tok.nextToken() == '}')break;
			else tok.pushBack();
			
			if(tok.nextToken() == StreamTokenizer.TT_WORD){
				String name = tok.sval;
				Topic t = new Topic(name,p.resolve(name+".topic"));
				if(topicsByName.containsKey(name)){
					throw new BotConfigException("topic already exists: "+t.name);
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
		// read the configuration data
		parseConfig(path);

		Logger.log("Created bot OK");
	}
}
