package org.pale.simplechat.actions;

import java.util.Iterator;
import java.util.List;

import org.pale.simplechat.actions.Runtime.LoopIterator;

public class ListLoopIterator implements LoopIterator {
	private Iterator<Value> iter;
	private Value cur=null;
	
	
	// this will construct the iterator, leaving it "unstarted".
	// The first "next" will set current to the first item in the iterable,
	// if there is one.
	ListLoopIterator(List<Value> lst){
		iter = lst.iterator();
	}

	// this will set to the next (or perhaps first) item in the iterable.
	@Override
	public void next() {
		if(hasNext())
			cur = iter.next();
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
