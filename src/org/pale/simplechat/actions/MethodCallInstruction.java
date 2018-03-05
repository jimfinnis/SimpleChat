package org.pale.simplechat.actions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.pale.simplechat.Conversation;
import org.pale.simplechat.Logger;

public class MethodCallInstruction extends Instruction {
	private Method method;
	private String name;
	public MethodCallInstruction(String name, Method method) {
		this.name = name;
		this.method = method;
	}

	@Override
	public int execute(Conversation c) {
		try {
			method.invoke(null, c);
		} catch (IllegalAccessException | IllegalArgumentException e) {
			Logger.log("Error in invocation of "+name+", exception is "+
				e.getClass().getSimpleName()+", message "+e.getMessage());
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			Throwable t = e.getCause();
			Logger.log("Command "+name+" caused an error: "+t.getClass().getSimpleName()+", "+t.getMessage());
			t.printStackTrace();
		}
		return 1;
	}

}
