package org.pale.simplechat.commands;

import org.pale.simplechat.Conversation;
import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.Cmd;
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
			System.out.println(s);
			s = s.replaceAll("([^\\.,;\\?\\!])\\z", "$1.");
			System.out.println(s);
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
		c.push(new StringValue(r));
		sentence(c);
	}

}
