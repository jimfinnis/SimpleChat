package org.pale.simplechat.actions;

import java.lang.reflect.InvocationTargetException;

import org.pale.simplechat.Conversation;

public class FuncCallInstruction extends Instruction {
	Function f;
	
	FuncCallInstruction(Function f){
		this.f = f;
	}
	@Override
	int execute(Conversation c) throws ActionException {
		try {
			f.run(c);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
			throw new ActionException("error occurred in call of '"+f.name+"' : "+e.toString());
		}
		return 1;
	}

}
