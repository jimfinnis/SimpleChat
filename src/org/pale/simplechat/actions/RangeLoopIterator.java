package org.pale.simplechat.actions;

import org.pale.simplechat.actions.Runtime.LoopIterator;
import org.pale.simplechat.values.IntValue;
import org.pale.simplechat.values.RangeValue;

public class RangeLoopIterator implements LoopIterator {
	RangeValue v;
	IntValue out;
	int cur;
	
	public RangeLoopIterator(RangeValue v){
		this.v = v;
		cur = v.start - 1; // starts BEFORE the beginning
	}
	
	@Override
	public void next() {
		if(hasNext()){
			cur++;
		}
	}

	@Override
	public boolean hasNext() {
		return cur < v.end-1;
	}

	@Override
	public Value current() {
		return new IntValue(cur);
	}

}
