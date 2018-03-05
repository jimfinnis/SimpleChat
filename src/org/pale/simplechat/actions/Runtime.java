package org.pale.simplechat.actions;
import java.util.List;
import java.util.Stack;

import org.pale.simplechat.Logger;
import org.pale.simplechat.Pair;

public class Runtime {
	private Stack<Value> stack = new Stack<Value>();
	
	public Value pop() throws ActionException{
		if(stack.empty())
			throw new ActionException("stack underflow");
		return stack.pop();
	}
	
	public void push(Value v) throws ActionException {
		if(stack.size()>100) // some kind of limit
			throw new ActionException("stack overflow");
		stack.push(v);
	}
	
	public List<Pair> popSubpats() throws ActionException {
		Value v = pop();
		if(v.t != Value.Type.SUBPATS)
			throw new ActionException("requires a subpats set on the stack");
		return v.subpats;
	}
	

	public String popString() throws ActionException {
		return pop().str();
	}
	
	public String getResult(){
		try {
			String s = pop().str();
			if(!stack.empty()){
				Logger.log("oops - stuff still on the stack (depth is "+stack.size()+")");
			}
			return s;
		} catch (ActionException e) {
			return "no result from action";
		}
	}
}
