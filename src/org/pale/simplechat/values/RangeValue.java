package org.pale.simplechat.values;

import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.BinopInstruction.Type;
import org.pale.simplechat.actions.RangeLoopIterator;
import org.pale.simplechat.actions.Runtime.LoopIterator;
import org.pale.simplechat.actions.Value;

public class RangeValue extends Value {
	public int start;
	public int end;

	public RangeValue(int start,int end){
		this.start = start;
		this.end = end;
	}

	@Override
	public Value binop(Type t, Value snd) {
		return null;
	}
	
	@Override public boolean equals(Object ob){
		if(this == ob)return true;
		if(!(ob instanceof RangeValue))return false;
		RangeValue b = (RangeValue)ob;
		return b.start == start && b.end==end;
	}
	
	@Override public String str(){
		return "<<RANGE: "+start+" to "+end+">>";
	}
	
	@Override
	public LoopIterator makeIterator() throws ActionException {
		return new RangeLoopIterator(this);
	}
}
