package org.pale.simplechat.values;

import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.Value;
import org.pale.simplechat.actions.BinopInstruction.Type;

public class StringValue extends Value {
	String s;
	
	public StringValue(String s){
		this.s = s;
	}
	@Override public boolean equals(Object ob){
		if(this == ob)return true;
		if(!(ob instanceof StringValue))return false;
		StringValue b = (StringValue)ob;
		return b.s.equals(s);
	}
	
	@Override public int toInt(){
		return Integer.parseInt(s);
	}
	
	@Override public double toDouble(){
		return Double.parseDouble(s);
	}
	
	@Override public String str(){
		return s;
	}
	@Override
	public Value binop(Type t, Value snd) {
		if(t == Type.ADD)
			return new StringValue(s+snd.str());
		else
			return null;
	}
	
	@Override
	public int size() throws ActionException {
		return s.length();
	}


}
