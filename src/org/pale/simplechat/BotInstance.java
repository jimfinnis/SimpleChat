package org.pale.simplechat;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.ActionLog;
import org.pale.simplechat.actions.Value;
import org.pale.simplechat.values.NoneValue;

/**
 * An actual chatting entity, which is backed by a Bot. There may be many BotInstances for one bot;
 * they will all talk the same way but may have different variables set.
 * @author white
 *
 */
public class BotInstance  {
	public Bot bot;
	
	// this is a map of each instance to each possible conversational partner.
	private Map<Object,Conversation> conversations = new HashMap<Object,Conversation>();

	private Object data; // data connected to the bot instance, could be anything
	
	public BotInstance(Bot b) throws BotConfigException{
		bot = b;
		try {
			// during the init action, the instance is "talking to itself" as it were.
			if(b.initAction!=null)
				b.initAction.run(new Conversation(this,this),true);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new BotConfigException("error running initialisation action: "+e.getMessage());
		} catch (ActionException e) {
			ActionLog.show();
			throw new BotConfigException("error running initialisation action: "+e.getMessage());
		}
	}
	
	// use this ctor when you want to connect some other data to the instance.
	public BotInstance(Bot b, Object data) throws BotConfigException{
		this(b); // call the other ctor
		this.data = data;
	}
	
	/// variables private (haha) to this instance.
	private Map<String,Value> vars = new HashMap<String,Value>();
	
	public Value getVar(String s){
		if(vars.containsKey(s))
			return vars.get(s);
		else
			return NoneValue.instance;
	}
	
	public void setVar(String s,Value v){
		vars.put(s, v);
	}
	
	// return the private data object you may have set; you'll need to cast.
	public Object getData(){
		return data;
	}
	
	public Conversation getConversation(Object source){
		// make a new conversation or get the existing one for this source
		Conversation c;
		if(conversations.containsKey(source))
			c = conversations.get(source);
		else {
			c = new Conversation(this,source);
			conversations.put(source, c);
		}
		return c;
	}
	
	/**
	 * Handle an input string from a user
	 * @param s the input
	 * @param source something associated with the user
	 * @return
	 */
	public String handle(String s,Object source){
		Conversation c = getConversation(source);
		
		// run any regex substitutions
		s = bot.subs.process(s);
		Logger.log("after subs: "+s);
		

		// pass through to the conversation
		return c.handle(s);
	}
	
	/**
	 * Run a function defined in the bot (typically used for events rather than conversational input)
	 * @param s the function name
	 * @param source a source object, typically the performer of an action on the bot.
	 * @return
	 * @throws ActionException 
	 */
	public String runFunc(String s,Object source) {
		Conversation c = getConversation(source);
		try {
			c.reset(); // reset (or create) the stacks etc.
			return c.runFunc(s);
		} catch (ActionException e) {
			Logger.log(e.getMessage());
			return "??"; // not ideal.
		}
	}
}
