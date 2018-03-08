package org.pale.simplechat.actions;
import java.util.List;
import java.util.Stack;

import org.pale.simplechat.Logger;
import org.pale.simplechat.Pair;
import org.pale.simplechat.values.ListValue;
import org.pale.simplechat.values.NoneValue;
import org.pale.simplechat.values.SubPatValue;

public class Runtime {
	// this interface will hold iterator data for iterable things.
	interface LoopIterator {
		// empty for the moment
	}
	
	private Stack<Value> stack; // the main stack
	public boolean exitflag; // set to quit code early. Cleared at start of run.
	public int totalinsts; // number of instructions run.
	// stack of iterators
	public Stack<LoopIterator> iterStack;
	
	// clears everything at the start of a top-level call
	public void reset(){
		iterStack = new Stack<LoopIterator>();
		stack = new Stack<Value>();
		exitflag = false;
		totalinsts = 0;
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
		if(!(v instanceof SubPatValue))
			throw new ActionException("requires a subpats set on the stack");
		return ((SubPatValue)v).subpats;
	}
	
	public List<Value> popList() throws ActionException {
		Value v = pop();
		if(!(v instanceof ListValue))
			throw new ActionException("requires a list on the stack");
		return ((ListValue)v).list;
	}
	

	public String popString() throws ActionException {
		return pop().str();
	}
	
	// return the result left on the stack. It will be converted to a string,
	// unless it is none, in which case null will be returned.
	public String getResult(){
		try {
			Value v = pop();
			if(v.equals(NoneValue.instance))return null;
			String s = v.str();
			if(!stack.empty()){
				Logger.log("oops - stuff still on the stack (depth is "+stack.size()+")");
			}
			return s;
		} catch (ActionException e) {
			return "no result from action";
		}
	}
}
