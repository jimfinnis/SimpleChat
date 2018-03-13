package org.pale.simplechat;

import java.io.StreamTokenizer;
import java.nio.file.Path;

@SuppressWarnings("serial")
public class BotConfigException extends Exception {
	public BotConfigException(Path p,StreamTokenizer tok, String s){
		super(p.getFileName()+":"+tok.lineno()+" : "+s);
	}

	public BotConfigException(Path p, String s){
		super(p.toAbsolutePath().toString()+": "+s);
	}
	
	// for the few cases where there is no file data
	public BotConfigException(String s){
		super(s);
	}
}
