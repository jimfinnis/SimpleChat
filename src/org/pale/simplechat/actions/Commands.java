package org.pale.simplechat.actions;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.pale.simplechat.Conversation;
import org.pale.simplechat.Logger;
import org.pale.simplechat.Topic;
import org.pale.simplechat.values.DoubleValue;
import org.pale.simplechat.values.IntValue;
import org.pale.simplechat.values.ListValue;
import org.pale.simplechat.values.NoneValue;
import org.pale.simplechat.values.StringValue;

public class Commands {

	/*
	 * Stack manipulation	
	 */
	@Cmd public static void dup(Conversation c) throws ActionException{
		c.push(c.peek());
	}
	
	@Cmd public static void drop(Conversation c) throws ActionException{
		c.pop();
	}
	
	@Cmd public static void swap(Conversation c) throws ActionException {
		Value a = c.pop();
		Value b = c.pop();
		c.push(a);
		c.push(b);
	}
	
	/*
	 * Debugging
	 */
	@Cmd public static void dp(Conversation c) throws ActionException{
		String s = c.pop().str();
		Logger.log("DP: "+s);
	}



	/*
	 * Type conversion
	 */
	@Cmd public static void str(Conversation c) throws ActionException {
		c.push(new StringValue(c.pop().str()));
	}

	@Cmd (name="int") public static void toint(Conversation c) throws ActionException {
		c.push(new IntValue(c.pop().toInt()));
	}
	@Cmd (name="double") public static void todouble(Conversation c) throws ActionException {
		c.push(new DoubleValue(c.pop().toDouble()));
	}
	
	/*
	 * None handling
	 */
	@Cmd public static void isnone(Conversation c) throws ActionException {
		c.push(new IntValue((c.pop().equals(NoneValue.instance))));
	}
	
	@Cmd public static void none(Conversation c) throws ActionException {
		c.push(NoneValue.instance);
	}

	/*
	 * String manipulation
	 */
	@Cmd public static void trim(Conversation c) throws ActionException{
		c.push(new StringValue(c.pop().str().trim()));
	}
	
	/*
	 * Pattern handling and topic manipulation
	 */
	@Cmd public static void next(Conversation c) throws ActionException{
		// tell the conversation to use the patterns we just specified to match first.
		c.specialpats =  c.popSubpats();
	}
	
	@Cmd public static void hasnext(Conversation c) throws ActionException {
		c.push(new IntValue(c.specialpats != null));
	}

	@Cmd public static void recurse(Conversation c) throws ActionException {
		// recurse the entire string (like SRAI in AIML)
		c.push(new StringValue(c.handle(c.popString())));
	}
	
	private static void doPromoteDemote(Conversation c,String name,boolean demote) throws ActionException{
		Topic t = c.instance.bot.getTopic(name);
		if(t!=null)
			c.promoteDemote(t, demote);
		else
			throw new ActionException("unknown topic: "+name);		
	}
	
	private static void doEnableDisableTopic(Conversation c,String name,boolean disable) throws ActionException{
		Topic t = c.instance.bot.getTopic(name);
		if(t!=null)
			c.enableDisableTopic(t, disable);
		else
			throw new ActionException("unknown topic: "+name);		
	}

	@Cmd public static void promote(Conversation c) throws ActionException {
		doPromoteDemote(c,c.popString(),false);
	}

	@Cmd public static void demote(Conversation c) throws ActionException {
		doPromoteDemote(c,c.popString(),true);
	}
	@Cmd public static void enabletopic(Conversation c) throws ActionException {
		doEnableDisableTopic(c,c.popString(),false);
	}

	@Cmd public static void disabletopic(Conversation c) throws ActionException {
		doEnableDisableTopic(c,c.popString(),true);
	}
	
	@Cmd public static void enablepattern(Conversation c) throws ActionException {
		// (topname patname -- )
		String patname = c.popString();
		String topname = c.popString();
		c.enableDisablePattern(topname, patname, false);
	}

	@Cmd public static void disablepattern(Conversation c) throws ActionException {
		// (topname patname -- )
		String patname = c.popString();
		String topname = c.popString();
		c.enableDisablePattern(topname, patname, true);
	}
	
	/*
	 * lists
	 */
	@Cmd public static void get(Conversation c) throws ActionException {
		// (idx list -- val)
		List<Value> lst = c.popList();
		int key = c.pop().toInt();
		if(key>=0 && key<lst.size())
			c.push(lst.get(key));
		else
			c.push(NoneValue.instance);
	}
	
	@Cmd public static void set(Conversation c) throws ActionException {
		// (val idx list -- )
		List<Value> lst = c.popList();
		int key = c.pop().toInt();
		Value v = c.pop();
		if(key>=0 && key<lst.size())
			lst.set(key, v);
	}
	
	@Cmd public static void pop(Conversation c) throws ActionException {
		// (list -- val)
		List<Value> lst = c.popList();
		if(lst.size()>0)
			c.push(lst.remove(lst.size()-1));
		else
			c.push(NoneValue.instance);
	}
	
	@Cmd public static void shift(Conversation c) throws ActionException {
		// (list -- val)
		List<Value> lst = c.popList();
		if(lst.size()>0)
			c.push(lst.remove(0));
		else
			c.push(NoneValue.instance);
	}

	@Cmd public static void push(Conversation c) throws ActionException {
		// (val list --)
		List<Value> lst = c.popList();
		Value v = c.pop();
		lst.add(v);
	}

	@Cmd public static void unshift(Conversation c) throws ActionException {
		// (list -- val)
		List<Value> lst = c.popList();
		Value v = c.pop();
		lst.add(0, v);
	}

	@Cmd public static void len(Conversation c) throws ActionException {
		c.push(new IntValue(c.popList().size()));
	}
	
	@Cmd public static void choose(Conversation c) throws ActionException {
		List<Value> lst = c.popList();
		int n = ThreadLocalRandom.current().nextInt(lst.size());
		c.push(lst.get(n));
	}
}
