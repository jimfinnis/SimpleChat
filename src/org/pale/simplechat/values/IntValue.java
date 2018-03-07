package org.pale.simplechat.values;

import org.pale.simplechat.actions.Value;

public class IntValue extends Value{
	int i;
	
	public IntValue(int i){
		this.i = i;
	}
	@Override public boolean equals(Object ob){
		if(this == ob)return true;
		if(!(ob instanceof IntValue))return false;
		IntValue b = (IntValue)ob;
		return b.i == i;
	}
	
	@Override public int toInt(){
		return i;
	}
	
	@Override public double toDouble(){
		return (double)i;
	}
	
	@Override public String str(){
		return Integer.toString(i);
	}
}
