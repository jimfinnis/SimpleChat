package org.pale.simplechat.values;

import org.pale.simplechat.actions.Value;

public class DoubleValue extends Value {
	double d;
	
	public DoubleValue(double d){
		this.d = d;
	}
	@Override public boolean equals(Object ob){
		if(this == ob)return true;
		if(!(ob instanceof DoubleValue))return false;
		DoubleValue b = (DoubleValue)ob;
		return b.d == d;
	}
	
	@Override public int toInt(){
		return (int)d;
	}
	
	@Override public double toDouble(){
		return d;
	}
	
	@Override public String str(){
		return Double.toString(d);
	}
}
