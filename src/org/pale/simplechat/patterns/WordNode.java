package org.pale.simplechat.patterns;

import org.pale.simplechat.Logger;
import org.pale.simplechat.Pattern;

/// simplest pattern node - just matches a word, case-insensitive. Parsed by a word.
public class WordNode extends Node {
	/**
	 * 
	 */
	private String word;

	public WordNode(Pattern pattern, String lab,Node parent,String word){
		super(pattern,lab, parent);
		this.word= word.toLowerCase();
		Logger.log(Logger.PATTERN,"New word: "+word);
	}
	@Override
	public void match(MatchData m) {
		log("entry, nodeword="+word+", inputword="+m.cur());
		if(m.invalid){log("early return");return;}
		if(m.cur().equals(word)){
			log("match succeeded: "+word);
			if(label!=null)m.setLabel(label,word);
			m.consumed = m.consume(); // eat the word and save it
		} else {
			m.invalid=true;
			log("match failed: "+word);
		}
	}
}