package org.pale.simplechat.actions;

import org.pale.simplechat.Conversation;

public class Commands {
	@Cmd public static void next(Conversation c) throws ActionException{
		// tell the conversation to use the patterns we just specified to match first.
		c.specialpats =  c.popSubpats();
	}
	
	@Cmd public static void recurse(Conversation c) throws ActionException {
		// recurse the entire string (like SRAI in AIML)
		c.push(new Value(c.handle(c.popString())));
	}
	
	@Cmd public static void str(Conversation c) throws ActionException {
		c.push(new Value(c.pop().str()));
	}

	@Cmd (name="int") public static void toint(Conversation c) throws ActionException {
		c.push(new Value(c.pop().toInt()));
	}
	@Cmd (name="double") public static void todouble(Conversation c) throws ActionException {
		c.push(new Value(c.pop().toDouble()));
	}
}
