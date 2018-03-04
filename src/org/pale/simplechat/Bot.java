package org.pale.simplechat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * encapsulates a bot - a style of conversation loaded from a file.
 * Individual entities which can be conversed with are BotInstance objects.
 * @author white
 *
 */

public class Bot {
	Map<String,Topic> topicsByName = new HashMap<String,Topic>();
	
	// each topic has a priority, and topics are tried in descending priority order. By
	// default the priority is 1. This is the map of default priority, loaded from the topic
	// files (topics can set their priority). It is copied to each new conversation, which
	// can increase or decrease priorities.
	Map<Topic,Double> topicPriorities = new HashMap<Topic,Double>();
	
	Topic main; // the main topic

	Substitutions subs; // substitutions to run before topics
	
	public Bot(Path path, Substitutions subs){
		main = new Topic(path,"main.topic",this);
		this.subs = subs;
		Logger.log("Created bot OK");
	}
	
	void addTopic(String name,Topic t,double weight){
		topicsByName.put(name, t);
		topicPriorities.put(t, weight);
	}
}
