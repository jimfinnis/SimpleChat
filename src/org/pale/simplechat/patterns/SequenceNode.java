package org.pale.simplechat.patterns;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.List;

import org.pale.simplechat.Bot;
import org.pale.simplechat.ParserError;
import org.pale.simplechat.Pattern;

/// a list of nodes which must all match. Parsed by ( .. )

public class SequenceNode extends Node {
	/**
	 * 
	 */
	private List<Node> nodes;
	
	public SequenceNode(Bot t,Pattern pattern, String lab, Node parent,StreamTokenizer tok) throws ParserError, IOException{
		super(pattern,lab, parent);
		nodes = this.pattern.parseNodeList(t,')',this,tok);
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
	public void match(MatchData m) {
		log("entry");
		if(m.invalid){log("early return");return;}
		StringBuilder out = new StringBuilder();
		// match IN ORDER
		for(Node n: nodes){
			n.match(m);
			if(m.invalid){
				log("failed");
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

