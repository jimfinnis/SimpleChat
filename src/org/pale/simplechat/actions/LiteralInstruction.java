package org.pale.simplechat.actions;

import org.pale.simplechat.Conversation;

public class LiteralInstruction extends Instruction {
	private Value val;
	LiteralInstruction(Value v){
		val = v;
	}
	
	@Override
	public int execute(Conversation c) throws ActionException {
		c.push(val);
		return 1;
	}

}
