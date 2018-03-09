package org.pale.simplechat.patterns;

import java.util.List;

import org.pale.simplechat.ParserError;
import org.pale.simplechat.Pattern;

/// has a list of nodes one of which must match. Parsed by [ ... ]
public class AnyOfNode extends Node {
	/**
	 * 
	 */
	private List<Node> nodes;
	public AnyOfNode(Pattern pattern, String lab) throws ParserError{
		super(pattern,lab);
		nodes = this.pattern.parseNodeList(']');
	}
	@Override
	public void match(MatchData m) {
		if(m.invalid){log("early return");return;}
		int op = m.pos; // save position
		for(Node n : nodes){
			// attempt to consume with this node
			n.match(m);
			if(!m.invalid){
				// we succeeded!
//TODO				if(label!=null)m.setLabel(label, word);
				log("match succeeded");
				// consumed will be set correctly by the child
				if(label!=null)m.setLabel(label, m.consumed);
				return;
			} else {
				log("submatch failed");
				m.invalid = false; // retry, clearing the invalid flag
			}
		}
		// if we got here, we failed.
		m.invalid = true;
		log("match failed");
		
	}
}