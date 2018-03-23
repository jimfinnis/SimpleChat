package org.pale.simplechat.patterns;

import java.io.IOException;
import java.io.StreamTokenizer;

import org.pale.simplechat.Bot;
import org.pale.simplechat.ParserError;
import org.pale.simplechat.Pattern;

public class StoredPatternNode extends Node {
	private String pname;
	private Pattern pat;

	public StoredPatternNode(Bot b, Pattern p, String lab, Node parent, StreamTokenizer tok) throws ParserError, IOException {
		super(p, lab, parent);
		if(tok.nextToken()!=StreamTokenizer.TT_WORD)
			throw new ParserError("expected stored pattern name after &");
		this.pat = b.getStoredPattern(tok.sval);
		this.pname = tok.sval;
		if(this.pat==null)
			throw new ParserError("cannot find stored pattern "+tok.sval);
	}

	@Override
	public void match(MatchData m) {
		log("entry, storedpat: "+pname);
		if(m.invalid){log("early return");return;}	
		pat.match(m);
	}

}
