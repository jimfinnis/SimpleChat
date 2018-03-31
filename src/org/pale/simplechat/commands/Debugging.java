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
	@Cmd public static void debug(Conversation c) throws ActionException {
		c.debug = c.pop().toInt()==0 ? false : true;
	}

	@Cmd public static void setlog(Conversation c) throws ActionException {
		Logger.setLog(c.pop().toInt());
	}

}
