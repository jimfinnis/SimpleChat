package org.pale.simplechat.actions;

import org.pale.simplechat.Conversation;

public class GetVarInstruction extends Instruction {
	enum Type {
		PATVAR,CONVVAR, INSTVAR;
	}
	private String name;
	public Type type;
	
	public GetVarInstruction(String name,Type t){
		this.name = name;
		type = t;
	}
	@Override
	public int execute(Conversation c) throws ActionException {
		switch(type){
		case PATVAR:
			c.push(c.getPatVar(name)); 
			break;
		case CONVVAR:
			c.push(c.getVar(name));
			break;
		case INSTVAR:
			c.push(c.instance.getVar(name));
			break;
		default:break;
		}
		return 1;
	}
}
