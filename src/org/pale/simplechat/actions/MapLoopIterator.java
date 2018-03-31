package org.pale.simplechat.actions;

import java.util.Iterator;
import java.util.Map;

import org.pale.simplechat.actions.Runtime.LoopIterator;
import org.pale.simplechat.values.StringValue;

public class MapLoopIterator implements LoopIterator {
	private Iterator<String> iter;
	private Value cur=null;
	
	
	// this will construct the iterator, leaving it "unstarted".
	// The first "next" will set current to the first item in the iterable,
	// if there is one.
	public MapLoopIterator(Map<String,Value> map){
		iter = map.keySet().iterator();
	}

	// this will set to the next (or perhaps first) item in the iterable.
	@Override
	public void next() {
		if(hasNext())
			cur = new StringValue(iter.next());
	}

	@Override
	public boolean hasNext() {
		return iter.hasNext();
	}

	@Override
	public Value current() {
		return cur;
	}
}
