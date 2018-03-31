package org.pale.simplechat.actions;

import org.pale.simplechat.actions.Runtime.LoopIterator;




public abstract class Value implements Comparable<Value> {
	public String str() {
		return "??";
	}
	
	@Override
	public int compareTo(Value v){
		return str().compareTo(v.str());
	}
	
	public int toInt() throws ActionException{
		throw new ActionException("cannot convert value to int");
	}
	
	public double toDouble() throws ActionException{
		throw new ActionException("cannot convert value to double");
	}	
	
	// binops call this on their first operand.
	public abstract Value binop(BinopInstruction.Type t,Value snd);

	public LoopIterator makeIterator() throws ActionException {
		throw new ActionException("Type "+this.getClass().getSimpleName()+" is not iterable");
	}

	public Value neg() throws ActionException {
		throw new ActionException("cannot negate a "+this.getClass().getSimpleName());
	}

	public Value get(Value k) throws ActionException {
		throw new ActionException(this.getClass().getSimpleName()+" is not a collection (list or hash");
	}
	
	public void set(Value k,Value v) throws ActionException {
		throw new ActionException(this.getClass().getSimpleName()+" is not a collection (list or hash");		
	}
	
	public boolean containsKey(Value k) throws ActionException {
		throw new ActionException(this.getClass().getSimpleName()+" is not a collection (list or hash");		
		
	}

	public int size() throws ActionException {
		throw new ActionException("cannot find size of "+this.getClass().getSimpleName());
	}
	
}
