package org.pale.simplechat.actions;

import org.pale.simplechat.Conversation;

public class SetVarInstruction extends Instruction {
	private String name;
	
	SetVarInstruction(String name){
		this.name=name;
	}
	
	@Override
	int execute(Conversation c) throws ActionException {
		Value v = c.pop();
		c.setVar(name,v);
		return 1;
	}

}
