package org.pale.simplechat;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A list of phrases available to the bot as random text data for variety.
 * ^list = [...]
 * @author white
 *
 */
public class PhraseList {
	List<String> phrases = new ArrayList<String>();
	
	PhraseList(StreamTokenizer tok) throws ParserError, IOException{
		for(;;){
			int t = tok.nextToken();
			if(t == ']')break;
			else if(t==StreamTokenizer.TT_WORD || t=='\'' || t=='"'){
				String s = tok.sval.replaceAll("_", " ");
				phrases.add(s);
				Logger.log(Logger.CONFIG, "phrase: "+s);
			}
		}
	}
	
	public String random(){
		if(0==phrases.size())return "";
		int n = ThreadLocalRandom.current().nextInt(phrases.size());
		return phrases.get(n);
	}

}
