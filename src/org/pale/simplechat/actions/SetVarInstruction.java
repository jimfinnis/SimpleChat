package org.pale.simplechat.actions;

import org.pale.simplechat.Conversation;

public class SetVarInstruction extends Instruction {
	private String name;
	enum Type {
		CONVVAR,INSTVAR;
	}
	private Type type;
	SetVarInstruction(String name,Type t){
		this.name=name;
		this.type = t;
	}
	
	@Override
	int execute(Conversation c) throws ActionException {
		Value v = c.pop();
		switch(type){
		case CONVVAR:
			c.setVar(name,v);break;
		case INSTVAR:
			c.instance.setVar(name,v);break;
			default:break;
			
		}
		return 1;
	}

}
