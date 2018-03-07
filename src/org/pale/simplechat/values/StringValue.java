package org.pale.simplechat.values;

import org.pale.simplechat.actions.Value;

public class StringValue extends Value {
	String s;
	
	public StringValue(String s){
		this.s = s;
	}
	@Override public boolean equals(Object ob){
		if(this == ob)return true;
		if(!(ob instanceof Value))return false;
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
}
