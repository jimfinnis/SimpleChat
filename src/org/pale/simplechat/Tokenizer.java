package org.pale.simplechat;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Tokenizer extends StreamTokenizer {
	public Tokenizer(Reader s){
		super(s);
		setDefault();
	}

	public Tokenizer(Path p) throws IOException{
		this(Files.newBufferedReader(p,StandardCharsets.UTF_8));
	}
	
	// when tokenizing categories, everything apart from spaces is a word char.
	public Tokenizer setForCats(){
		resetSyntax();
		wordChars(0x21, 0xFF);
		ordinaryChar('=');
		ordinaryChar('[');
		ordinaryChar(']');
		ordinaryChar('~');
		whitespaceChars(0x00, 0x20);
		quoteChar('"');
		quoteChar('\'');
		commentChar('#');
		return this;
	}
	
	// the standard tokenizer setup
	public Tokenizer setDefault(){
		resetSyntax();
		wordChars('a', 'z');
		wordChars('A', 'Z');
		wordChars('\u00A0', '\u00FF');
		whitespaceChars(0x00, 0x20);
		quoteChar('"');
		quoteChar('\'');
		parseNumbers();
		commentChar('#');
		ordinaryChar('/');
		ordinaryChar('.');
		wordChars('_', '_');

		return this;
	}


}
