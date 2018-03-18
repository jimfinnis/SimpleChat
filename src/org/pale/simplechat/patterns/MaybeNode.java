package org.pale.simplechat.patterns;

import java.io.IOException;
import java.io.StreamTokenizer;

import org.pale.simplechat.Bot;
import org.pale.simplechat.ParserError;
import org.pale.simplechat.Pattern;

public class MaybeNode extends Node {

	Node node;
	public MaybeNode(Bot b,Pattern p, String lab,Node parent, StreamTokenizer tok) throws ParserError, IOException {
		super(p, lab,parent);
		node = p.parseNode(b,this,tok);
	}
	
	@Override
	public void nextLinkChild(Node n){
		node.next = n;
	}

	@Override
	public void match(MatchData m) {
		log("MaybeNode");
		if(m.invalid){log("early return");return;}

		// try to match the node
		node.match(m);
		// if we got it, handle it.
		if(!m.invalid){
			if(label!=null){
				m.setLabel(label, m.consumed);
			}
		} else {
			m.consumed="";
			// otherwise just clear the state and move on..
			log("failed");
			m.invalid = false;
		}
	}

}
