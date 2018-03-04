package org.pale.simplechat.actions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.pale.simplechat.Conversation;

public class MethodCallInstruction implements Instruction {
	Method method;
	
	public MethodCallInstruction(Method method) {
		this.method = method;
	}

	@Override
	public void execute(Conversation c) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		method.invoke(null, c);

	}

}
