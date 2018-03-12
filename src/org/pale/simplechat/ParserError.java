package org.pale.simplechat;

// general parse error; should get converted to a BotConfigException at a level where we have
// file data.
@SuppressWarnings("serial")
public class ParserError extends Exception {
	public ParserError(String s){
		super(s);
	}
}
