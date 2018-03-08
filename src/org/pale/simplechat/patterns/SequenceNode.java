package org.pale.simplechat.patterns;

import java.util.List;

import org.pale.simplechat.ParserError;
import org.pale.simplechat.Pattern;

/// a list of nodes which must all match. Parsed by ( .. )

public class SequenceNode extends Node {
	/**
	 * 
	 */
	private List<Node> nodes;
	
	public SequenceNode(Pattern pattern, String lab) throws ParserError{
		super(pattern,lab);
		nodes = this.pattern.parseNodeList(')');
		Node pnode=null;
		// link the child nodes together into a linked list
		for(Node n: nodes){
			if(pnode!=null){
				pnode.next = n;
				pnode.nextLinkChild(n);
			}
			pnode = n;
		}
	}

	@Override
	public void parse(MatchData m) {
		StringBuilder out = new StringBuilder();
		if(m.invalid){log("early return");return;}
		// match IN ORDER
		for(Node n: nodes){
			n.parse(m);
			if(m.invalid){
				log("match failed");
				return; // failed
			}
			if(m.consumed.length()>0)
				out.append(m.consumed+" ");
		}
		m.consumed = out.toString().trim();
		if(label!=null)m.setLabel(label, m.consumed);
		log("match succeeded");
	}
}

