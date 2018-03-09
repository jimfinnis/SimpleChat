package org.pale.simplechat.patterns;

import org.pale.simplechat.Pattern;


/// for debugging
public class DummyNode extends Node {
	private String data;
	public DummyNode(Pattern pattern,String lab,String s){
		super(pattern,lab);
		data = s;
	}
	@Override
	public void match(MatchData m) {
		// consumes no tokens, does nothing.
	}
}