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
		commentChar('#');
		ordinaryChar('/');
		ordinaryChar('.');
		wordChars('_', '_');
	}

	public Tokenizer(Path p) throws IOException{
		this(Files.newBufferedReader(p,StandardCharsets.UTF_8));
	}
}
