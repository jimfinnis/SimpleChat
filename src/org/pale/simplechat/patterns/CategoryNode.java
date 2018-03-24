package org.pale.simplechat.patterns;

import java.io.IOException;
import java.io.StreamTokenizer;

import org.pale.simplechat.Bot;
import org.pale.simplechat.Category;
import org.pale.simplechat.ParserError;
import org.pale.simplechat.Pattern;

public class CategoryNode extends Node {
	private Category c;
	String cname;
	public CategoryNode(Bot b,Pattern pattern, String label,Node parent, StreamTokenizer tok) throws ParserError, IOException {
		super(pattern,label,parent);
		if(tok.nextToken()!=StreamTokenizer.TT_WORD)
			throw new ParserError("expected a category name after ~ in pattern");
		this.cname = tok.sval;
		c = b.getCategory(cname,true);
		if(c==null)
			throw new ParserError("cannot find category ~"+cname);
	}

	@Override
	public void match(MatchData m) {
		log("entry, cat: "+cname);
		if(m.invalid){log("early return");return;}
		// lesser of several evils, putting matching into Category - putting it there
		// moves some match code out of .patterns, but adds a dependency on MatchData into
		// Category. Keeping it here keeps the match code all in one package, but exposes
		// a lot of Category because of the shenanigans when matching has to recurse down
		// the category tree.
		if(!c.match(m)){
			log("failed");
			m.invalid=true;
		} else 
			m.setLabel(label, m.consumed);

	}



}
