package org.pale.simplechat.commands;

import org.pale.simplechat.Conversation;
import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.Cmd;
import org.pale.simplechat.actions.Value;

public class Stack {

	@Cmd public static void drop(Conversation c) throws ActionException{
		c.pop();
	}

	@Cmd public static void dup(Conversation c) throws ActionException{
		c.push(c.peek());
	}

	@Cmd public static void swap(Conversation c) throws ActionException {
		Value a = c.pop();
		Value b = c.pop();
		c.push(a);
		c.push(b);
	}

}
