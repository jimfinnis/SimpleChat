package org.pale.simplechat.commands;

import java.lang.reflect.InvocationTargetException;

import org.pale.simplechat.Conversation;
import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.Cmd;
import org.pale.simplechat.actions.Function;

public class Other {
	// (string -- ) call a named function (which itself might manipulate the stack a lot more!)
	@Cmd public static void call(Conversation c) throws ActionException {
		String s = c.popString();
		Function f = c.instance.bot.getFunc(s);
		if(f==null)
			throw new ActionException("bot does not declare function "+s);
		try {
			f.run(c);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new ActionException("Error in "+s+": "+e.getMessage());
		}
	}
}
