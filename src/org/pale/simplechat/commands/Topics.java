package org.pale.simplechat.commands;

import org.pale.simplechat.Conversation;
import org.pale.simplechat.Topic;
import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.Cmd;
import org.pale.simplechat.values.IntValue;
import org.pale.simplechat.values.NoneValue;
import org.pale.simplechat.values.StringValue;

public class Topics {

	
	@Cmd public static void next(Conversation c) throws ActionException{
		// tell the conversation to use the patterns we just specified to match first.
		c.specialpats =  c.popSubpats();
	}

	@Cmd public static void hasnext(Conversation c) throws ActionException {
		c.push(new IntValue(c.specialpats != null));
	}
	
	// tell the system NOT to throw away the special patterns!
	@Cmd public static void holdnext(Conversation c) throws ActionException {
		c.holdnext();
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
		Topics.doEnableDisableTopic(c,c.popString(),true);
	}
	
	@Cmd public static void enablepattern(Conversation c) throws ActionException {
		// (patname topname -- )
		String topname = c.popString();
		String patname = c.popString();
		c.enableDisablePattern(topname, patname, false);
	}

	@Cmd public static void disablepattern(Conversation c) throws ActionException {
		// (patname topname -- )
		String topname = c.popString();
		String patname = c.popString();
		c.enableDisablePattern(topname, patname, true);
	}
	
	@Cmd public static void curtopic(Conversation c) throws ActionException {
		Topic t = c.getCurTopic();
		if(t==null)
			c.push(NoneValue.instance);
		else
			c.push(new StringValue(t.name));
	}
}
