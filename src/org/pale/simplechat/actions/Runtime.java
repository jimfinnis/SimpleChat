package org.pale.simplechat.actions;
import java.util.List;
import java.util.Stack;

import org.pale.simplechat.Logger;
import org.pale.simplechat.Pair;

public class Runtime {
	// this interface will hold iterator data for iterable things.
	interface LoopIterator {
		// empty for the moment
	}
	
	private Stack<Value> stack; // the main stack
	public boolean exitflag; // set to quit code early. Cleared at start of run.
	// stack of iterators
	public Stack<LoopIterator> iterStack;
	
	public void reset(){
		iterStack = new Stack<LoopIterator>();
		stack = new Stack<Value>();
		exitflag = false;
	}
	
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
	
	public Value peek() throws ActionException {
		if(stack.empty())
			throw new ActionException("stack underflow");
		return stack.peek();
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
