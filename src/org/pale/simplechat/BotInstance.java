package org.pale.simplechat;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.ActionLog;
import org.pale.simplechat.actions.Value;

/**
 * An actual chatting entity, which is backed by a Bot. There may be many BotInstances for one bot;
 * they will all talk the same way but may have different variables set.
 * @author white
 *
 */
public class BotInstance extends Source { // extends Source so bots can talk to each other, maybe.
	public Bot bot;
	
	// this is a map of each instance to each possible conversational partner.
	private Map<Source,Conversation> conversations = new HashMap<Source,Conversation>();
	
	public BotInstance(Bot b) throws BotConfigException{
		bot = b;
		try {
			// during the init action, the instance is "talking to itself" as it were.
			if(b.initAction!=null)
				b.initAction.run(new Conversation(this,this));
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new BotConfigException("error running initialisation action: "+e.getMessage());
		} catch (ActionException e) {
			ActionLog.show();
			throw new BotConfigException("error running initialisation action: "+e.getMessage());
		}
	}
	
	/// variables private (haha) to this instance.
	private Map<String,Value> vars = new HashMap<String,Value>();
	
	public Value getVar(String s){
		if(vars.containsKey(s))
			return vars.get(s);
		else
			return new Value("??");
	}
	
	public void setVar(String s,Value v){
		vars.put(s, v);
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
