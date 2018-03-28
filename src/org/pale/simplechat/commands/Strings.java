package org.pale.simplechat.commands;

import org.pale.simplechat.Conversation;
import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.Cmd;
import org.pale.simplechat.utils.NumberUtils;
import org.pale.simplechat.values.StringValue;

public class Strings {

	
	@Cmd public static void trim(Conversation c) throws ActionException{
		c.push(new StringValue(c.pop().str().trim()));
	}

	@Cmd public static void sentence(Conversation c) throws ActionException {
		String s = c.popString();
		if(s.length()>0){
			s = s.substring(0,1).toUpperCase()+s.substring(1);
			// if the string doesn't end with a punct, add a "."
			s = s.replaceAll("([^\\.,;\\?\\!])\\z", "$1.");
		}
		c.push(new StringValue(s));
	}
	
	@Cmd public static void clean(Conversation c) throws ActionException {
		String r = c.popString();
		r = r.replaceAll("\\s+", " "); // replace mult. whitespace with space
		// replace "xword" with "x word" where x is a punc.
		r = r.replaceAll("([\\.,;\\?\\!])(\\w)", "$1 $2");
		// replace "  punc" with "punc"
		r = r.replaceAll("\\s+([\\.,;\\?\\!])", "$1");
		// replace "puncLetter" with "punc Letter"
		r = r.replaceAll("([\\.,;\\?\\!])(\\w)", "$1 $2");
		
		c.push(new StringValue(r));
		sentence(c);
	}
	
	// (noun -- a|an) returns correct indefinite article
	@Cmd public static void article(Conversation c) throws ActionException {
		String s = c.popString();
		if(s.length()>0 && "aeiou".indexOf(s.charAt(0))>=0)
			s = "an";
		else
			s = "a";
		c.push(new StringValue(s));
	}
	
	// (noun -- a|an noun) adds article if it doesn't have one
	@Cmd public static void addarticle(Conversation c) throws ActionException {
		String s = c.popString();
		if(s.length()>3 && (s.substring(0,2).equals("a ") || s.substring(0,3).equals("an ")))
			c.push(new StringValue(s)); // already has one
		
		if(s.length()>0 && "aeiou".indexOf(s.charAt(0))>=0)
			s = "an "+s;
		else
			s = "a "+s;
		c.push(new StringValue(s));
	}

	// (ct string -- string plus "s" if ct>1) noun pluralizer
	@Cmd public static void pluralise(Conversation c) throws ActionException {
		String s = c.popString();
		int ct = c.pop().toInt();
		if(ct>1)c.push(new StringValue(s+"s"));
		else c.push(new StringValue(s));
	}

	// (ct string -- ct + string + "s" if ct>1) crude noun pluralizer which includes number
	@Cmd public static void pluralisenum(Conversation c) throws ActionException {
		String s = c.popString();
		int ct = c.pop().toInt();
		String numstr = NumberUtils.numberToWords(ct);
		if(ct>1)numstr = numstr+" "+s+"s";
		else numstr = numstr+" "+s;
		c.push(new StringValue(numstr));
	}
	
	// (n -- string) convert number to english words
	@Cmd public static void englishnum(Conversation c) throws Exception {
		 int n = c.pop().toInt();
		 c.push(new StringValue(NumberUtils.numberToWords(n)));
		 
	 }

}
