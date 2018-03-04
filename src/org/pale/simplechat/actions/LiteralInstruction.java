package org.pale.simplechat.actions;

import org.pale.simplechat.Conversation;

public class LiteralInstruction implements Instruction {
	private Value val;
	LiteralInstruction(Value v){
		val = v;
	}
	
	@Override
	public void execute(Conversation c) throws ActionException {
		c.push(val);
	}

}
