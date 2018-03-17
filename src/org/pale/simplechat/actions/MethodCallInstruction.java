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
	public int execute(Conversation c) throws ActionException {
		try {
			method.invoke(null, c);
		} catch (IllegalAccessException | IllegalArgumentException e) {
			Logger.log(Logger.ACTION+Logger.FATAL,"Error in invocation of "+name+", exception is "+
				e.getClass().getSimpleName()+", message "+e.getMessage());
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			Throwable t = e.getCause();
			e.printStackTrace();
			Logger.log(Logger.ACTION+Logger.FATAL,"Command "+name+" caused an error: "+t.getClass().getSimpleName()+", "+t.getMessage());
			throw new ActionException("Command "+name+" caused an error: "+t.getClass().getSimpleName()+", "+t.getMessage());
			
		}
		return 1;
	}
	
	@Override public String toString(){
		return "MethodCallInstruction:"+name;
	}


}
