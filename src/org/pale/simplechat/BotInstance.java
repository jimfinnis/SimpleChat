package org.pale.simplechat;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.ActionLog;
import org.pale.simplechat.actions.Value;
import org.pale.simplechat.values.NoneValue;
import org.pale.simplechat.values.StringValue;

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
	
	/**
	 * Construct a new instance of a bot.
	 * @param b the bot we're going to use.
	 * @param name the name of the bot (used to set the botname instance var, nothing else).
	 * @throws BotConfigException
	 */
	public BotInstance(Bot b, String name) throws BotConfigException{
		bot = b;
		try {
			// go up the tree, running all the init instances
			// during the init action, the instance is "talking to itself" as it were.
			Conversation c = new Conversation(this,this);
			vars.put("botname",new StringValue(name));
			b.runInits(c);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new BotConfigException("error running initialisation action: "+e.getMessage());
		} catch (ActionException e) {
			ActionLog.show();
			throw new BotConfigException("error running initialisation action: "+e.getMessage());
		}
		bot.instances.add(this);
	}
	
	// use this ctor when you want to connect some other data to the instance.
	// YES I KNOW it's a cut and paste of the other. When 'data' is present, this
	// needs to be set FIRST in the instance. Can't do that with this()..
	public BotInstance(Bot b, String name, Object data) throws BotConfigException{
		bot = b;
		this.data = data;
		try {
			// go up the tree, running all the init instances
			// during the init action, the instance is "talking to itself" as it were.
			Conversation c = new Conversation(this,this);
			vars.put("botname",new StringValue(name));
			b.runInits(c);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new BotConfigException("error running initialisation action: "+e.getMessage());
		} catch (ActionException e) {
			ActionLog.show();
			throw new BotConfigException("error running initialisation action: "+e.getMessage());
		}
		bot.instances.add(this);
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
		s = bot.processSubs(s);
		Logger.log(Logger.PATTERN,"after subs: "+s);
		

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
			Logger.log(Logger.FATAL,e.getMessage());
			return "??"; // not ideal.
		}
	}
	
	public void remove(){
		bot.instances.remove(this);
	}
}
