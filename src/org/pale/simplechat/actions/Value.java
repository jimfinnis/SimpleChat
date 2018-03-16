package org.pale.simplechat.actions;

import org.pale.simplechat.actions.Runtime.LoopIterator;




public abstract class Value {
	public String str() {
		return "??";
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
	
}
