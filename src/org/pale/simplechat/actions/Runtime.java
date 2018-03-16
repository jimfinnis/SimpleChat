package org.pale.simplechat.actions;
import java.util.List;
import java.util.Stack;

import org.pale.simplechat.Pair;
import org.pale.simplechat.values.ListValue;
import org.pale.simplechat.values.SubPatValue;

public class Runtime {

	protected Stack<Value> stack; // the main stack
	public boolean exitflag; // set to quit code early. Cleared at start of run.
	public int totalinsts; // number of instructions run.
	
	/// a simple iterator interface - we don't use Iterator because it doesn't handle ranges
	/// and doesn't have current().
	public interface LoopIterator {
		void next();
		boolean hasNext();
		Value current();
	}
	
	// stack of iterators. Annoyingly, Stack doesn't provide peek(int)
	public Stack<LoopIterator> iterStack;
	
	// Annoyingly, Stack (and Deque) doesn't provide peek(int). This returns the nth item from the top.
	// so iterStackPeek(0) = iterStack.peek().
	public LoopIterator iterStackPeek(int n) throws ActionException{
		if(n<=iterStack.size()){
			return iterStack.get(iterStack.size()-(n+1));
		}
		else throw new ActionException("not enough nested loops");
	}
	
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
	
	public String stackDump(){
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<stack.size();i++)
			sb.append(stack.get(i).str()+" | ");
		return sb.toString();
	}
	
}
