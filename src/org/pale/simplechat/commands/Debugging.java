package org.pale.simplechat.commands;

import org.pale.simplechat.Conversation;
import org.pale.simplechat.Logger;
import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.Cmd;

public class Debugging {

	/*
	 * Debugging
	 */
	@Cmd public static void dp(Conversation c) throws ActionException{
		String s = c.pop().str();
		Logger.log(Logger.ALWAYS,"DP: "+s);
	}

}
