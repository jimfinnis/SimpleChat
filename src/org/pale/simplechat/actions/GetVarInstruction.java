package org.pale.simplechat.actions;

import java.lang.reflect.InvocationTargetException;

import org.pale.simplechat.Conversation;

public class GetVarInstruction implements Instruction {
	enum Type {
		PATVAR,CONVVAR;
	}
	private String name;
	public Type type;
	
	public GetVarInstruction(String name,Type t){
		this.name = name;
		type = t;
	}
	@Override
	public void execute(Conversation c) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			ActionException {
		switch(type){
		case PATVAR:
			c.push(new Value(c.getPatVar(name)));
			break;
		case CONVVAR:
			c.push(new Value(c.getVar(name)));
			break;
		default:break;
		}
	}
}
