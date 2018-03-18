package org.pale.simplechat.patterns;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.List;

import org.pale.simplechat.Bot;
import org.pale.simplechat.ParserError;
import org.pale.simplechat.Pattern;

/// has a list of nodes one of which must match. Parsed by [ ... ]
public class AnyOfNode extends Node {
	/**
	 * 
	 */
	private List<Node> nodes;
	public AnyOfNode(Bot b,Pattern pattern, String lab,Node parent,StreamTokenizer tok) throws ParserError, IOException{
		super(pattern,lab,parent);
		nodes = this.pattern.parseNodeList(b,']',this,tok);
	}
	@Override
	public void match(MatchData m) {
		log("entry");
		if(m.invalid){log("early return");return;}
		for(Node n : nodes){
			// attempt to consume with this node
			n.match(m);
			if(!m.invalid){
				// we succeeded!
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
		log("failed");
		
	}
}