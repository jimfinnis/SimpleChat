package org.pale.simplechat.commands;

import org.pale.simplechat.Conversation;
import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.Cmd;
import org.pale.simplechat.values.NoneValue;
import org.pale.simplechat.values.StringValue;

public class OutputString {
	
	
	@Cmd public static void out(Conversation c) throws ActionException {
		if(c.getOutput()==null)
			c.push(NoneValue.instance);
		else
			c.push(new StringValue(c.getOutput()));
	}
	@Cmd public static void clearout(Conversation c) throws ActionException {
		c.clearOutput();		
	}
}
