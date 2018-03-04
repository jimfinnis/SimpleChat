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
	String[] words; // words of the sentence
	int pos;	// first unparsed word
	public boolean invalid; // set when parse fails; may get temporarily cleared (e.g. in AnyOfNode)
	public String consumed; // the string most recently consumed
	public MatchData(String[] ss){
		words = ss;
		pos = 0;
		invalid = false;
	}
	
	String consume(){
		if(pos >= words.length)return "";
		else return words[pos++];
	}
	
	String cur(){
		if(pos < words.length)
			return words[pos];
		else
			return "??";
	}

	/// will only set label if it is a string of nz length
	public void setLabel(String label, String word) {
		if(word!=null && word.length()>0){
			Logger.log("Setting "+label+" to "+word);
			labels.put(label, word);
		}
	}

	public boolean allConsumed() {
		return pos == words.length;
	}

	public String consumeAll() {
		StringBuilder sb = new StringBuilder();
		while(pos<words.length){
			sb.append(words[pos++]+" ");
		}
		return sb.toString();
	}
}