package org.pale.simplechat.patterns;

import org.pale.simplechat.ParserError;
import org.pale.simplechat.Pattern;

public class NegateNode extends Node {

	private Node node; // the node we want to negate

	public NegateNode(Pattern p, String lab) throws ParserError {
		super(p, lab);
		pattern.iter.next();
		// parse the child pattern we want to negate
		node = pattern.parseNode();
	}
	
	@Override
	public void nextLinkChild(Node n){
		node.next = n;
	}

	@Override
	public void match(MatchData m) {
		if(m.invalid){log("early return");return;}

		// record the match data to reset once we verify the match failed
		int pos = m.pos;
		node.match(m); // try to parse the node
		// if it failed, that's OK, it's what we want. Just reset the pos and accept.
		m.consumed="";
		if(m.invalid){
			m.pos = pos;
			m.invalid = false;
		} else {
			// the node was accepted, and that's a fail.
			m.invalid = true;
		}
	}
}
