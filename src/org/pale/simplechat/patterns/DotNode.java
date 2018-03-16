package org.pale.simplechat.patterns;

import org.pale.simplechat.Pattern;

// matches one of any word
public class DotNode extends Node {

	public DotNode(Pattern p, String lab) {
		super(p, lab);
		p.iter.next();
	}

	@Override
	public void match(MatchData m) {
		log("entry");
		if(m.invalid){log("early return");return;}
		
		if(m.allConsumed()){
			log("failed, all consumed");
			m.invalid=true;
			return;
		}
		m.consumed = m.consume();
		if(label!=null)m.setLabel(label, m.consumed);
	}

}
