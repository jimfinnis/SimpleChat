package org.pale.simplechat;

import java.util.HashMap;
import java.util.Map;

/**
 * An actual chatting entity, which is backed by a Bot. There may be many BotInstances for one bot;
 * they will all talk the same way but may have different variables set.
 * @author white
 *
 */
public class BotInstance extends Source { // extends Source so bots can talk to each other, maybe.
	Bot bot;
	
	// this is a map of each instance to each possible conversational partner.
	private Map<Source,Conversation> conversations = new HashMap<Source,Conversation>();
	
	public BotInstance(Bot b){
		bot = b;
	}
	
	public String handle(String s,Source p){
		Conversation c;
		
		// run any regex substitutions
		s = bot.subs.process(s);
		Logger.log("after subs: "+s);
		
		// make a new conversation or get the existing one for this source
		if(conversations.containsKey(p))
			c = conversations.get(p);
		else {
			c = new Conversation(this,p);
			conversations.put(p, c);
		}
		// pass through to the conversation
		return c.handle(s);
	}
}
