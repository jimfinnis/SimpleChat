package org.pale.simplechat.patterns;

import java.util.HashMap;
import java.util.Map;

import org.pale.simplechat.Logger;

/**
 * Data about a match, both results and progress data used in matching; 
 * results consist mainly of labelled node contents,
 * progress data consists of position in input.
 * @author white
 *
 */
public class MatchData {
	// labelled results
	public Map<String,String> labels = new HashMap<String,String>();
	// progress data
	public String[] words; // words of the sentence in lower-cased form
	public String[] wordsRaw; // words of the sentence in native case
	public int pos;	// first unparsed word
	public boolean invalid; // set when parse fails; may get temporarily cleared (e.g. in AnyOfNode)
	public String consumed; // the string most recently consumed, in native case
	public MatchData(String[] ss){
		wordsRaw = ss;
		// produce lower-case version to match agains
		words = new String[ss.length];
		for(int i=0;i<ss.length;i++){
			words[i] = ss[i].toLowerCase();
		}
		pos = 0;
		invalid = false;
	}
	
	public String consume(){
		if(pos >= words.length)return "";
		else return wordsRaw[pos++];
	}
	
	public String cur(){
		if(pos < words.length)
			return words[pos];
		else
			return "??";
	}

	/// will only set label if it is a string of nz length
	public void setLabel(String label, String word) {
		if(word!=null && word.length()>0){
			Logger.log(Logger.PATTERN,"Setting "+label+" to "+word);
			labels.put(label, word);
		}
	}

	public boolean allConsumed() {
		return pos == words.length;
	}

	// returns the raw, not lowercased, versions.
	public String consumeAll() {
		StringBuilder sb = new StringBuilder();
		while(pos<words.length){
			sb.append(wordsRaw[pos++]+" ");
		}
		return sb.toString();
	}
}