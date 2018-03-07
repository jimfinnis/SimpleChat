package org.pale.simplechat.actions;




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
	
}
