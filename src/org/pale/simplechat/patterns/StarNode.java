package org.pale.simplechat.patterns;

import java.io.StreamTokenizer;

import org.pale.simplechat.ParserError;
import org.pale.simplechat.Pattern;

public class StarNode extends Node {
	private Node node;
	private int minCount,maxCount; // the number of matches must be in this range
	public StarNode(Pattern pattern, Node n,String lab,boolean atLeastOne,Node parent, StreamTokenizer tok) throws ParserError {
		super(pattern,lab,parent);
		node = n; // the node we are wrapping
		node.parent = this;
		minCount= atLeastOne ? 1 : 0;
		maxCount = 10000; // not used currently.
	}

	@Override
	public void match(MatchData m) {
		log("entry");
		if(m.invalid){log("early return");return;}
		
		// the idea is that "foo* bar" should keep trying to match "bar"
		// and if that fails match a "foo" and consume. IF we did this the greedy way,
		// just having it consume "foo" until it finished, we'd end up with problems processing
		// patterns like ".* foo" - the matcher would just eat tokens until the end.
		
		StringBuilder sb = new StringBuilder(); // build up the consumed tokens
		int n = 0; // count of matches
		while(!m.allConsumed()){
			// if there is one, try to match the next node. If there is no next node at this level,
			// go up a level and try to use the next node there.
			
			Node nextNode = getNextNode();

			
			if(nextNode!=null){
				log("attempting next node match, next node is "+nextNode.getClass().getSimpleName());
				int pos = m.pos;
				nextNode.match(m);
				m.pos = pos; // reset position, we'll always want to reparse this token
				if(!m.invalid){
					// we succeeded, so exit ready to parse that node
					log("next node succeeded, star node terminating");
					break;
				} else log("next node failed, continuing consumption");
			} else log("no next node, will consume all tokens");
			// otherwise, try to match our repeating node
			m.invalid =false; // clear match state
			node.match(m);
			if(m.invalid){
				log("failed, didn't match subnode");
				// didn't get it; exit, this is a failure
				return;
			} else
				sb.append(m.consumed+" ");
			n++;
		}
		if(n<minCount || n>maxCount){
			log("failed, too many or too few");
			m.invalid=true;
		} else {
			m.consumed=sb.toString().trim();
			if(label!=null){
				m.setLabel(label, m.consumed);
				// we also add the count
				m.setLabel(label+"_ct", Integer.toString(n));
			}
		}
	}
}
