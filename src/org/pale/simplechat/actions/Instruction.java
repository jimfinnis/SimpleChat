package org.pale.simplechat.actions;

import org.pale.simplechat.Conversation;
import org.pale.simplechat.ParserError;

public abstract class Instruction {
	/// run the instruction and return the next instruction's offset (usually 1 to increment)
	abstract int execute(Conversation c) throws ActionException;
	/// set the instruction's offset if it's a jump.
	void setJump(int offset) throws ParserError{
		throw new ParserError("weirdness - tried to set a jump offset on a non-jump instruction");
	}
}
