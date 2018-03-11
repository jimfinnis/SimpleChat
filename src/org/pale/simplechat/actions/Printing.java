package org.pale.simplechat.actions;

import org.pale.simplechat.Conversation;

public class Printing {

	public static class PrintInstruction extends Instruction {
		@Override
		int execute(Conversation c) throws ActionException {
			// just append to the current print string
			c.appendOutput(c.popString());
			return 1;
		}
	}

}
