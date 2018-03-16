package org.pale.simplechat;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * This object encapsulates common substitutions which can be made by regex on input
 * before being processed. Bots may be given one of these.
 * Within each file are lines of the form
 *  regex:subst
 * and each substitution is done in the order it appears in the file. Additionally,
 * the separator can be changed with "#sep character" (in case you need colons)
 * and files can be included with "#include file". Any other line starting with # is a comment.
 * @author white
 *
 */
public class Substitutions implements SubstitutionsInterface {
	public String process(String in){
		for(SubstPair p: substs){
			in = p.repl(in);
		}
		return in;
	}
	
	private class SubstPair {
		java.util.regex.Pattern p;
		String r;
		SubstPair(String p,String r){
			this.p = java.util.regex.Pattern.compile(p);
			this.r = r;
		}
		String repl(String in){
			return p.matcher(in).replaceAll(r);
		}
	}
	private List<SubstPair> substs = new ArrayList<SubstPair>();
	
	// separate to permit "include", if you want to do it that way
	private void parseFile(Path p,String file){
		Path f = p.resolve(file);
		String sep = ":";
		try {
			BufferedReader r = Files.newBufferedReader(f,StandardCharsets.UTF_8);
			String s;
			while((s = r.readLine())!=null){
				if(s.length()>1){
					if(s.substring(0, 1).equals("#")){
						String[] arr = s.substring(1).split("\\s+");
						if(arr.length>1){
							if(arr[0].equals("include"))
								parseFile(p,arr[1]);
							else if(arr[0].equals("sep"))
								sep =arr[1];
						}
					} else {
						String[] arr = s.split(sep);
						substs.add(new SubstPair(arr[0],arr[1]));						
					}
				}
			}
		} catch (IOException e) {
			Logger.log(Logger.FATAL,"cannot open file "+f.toAbsolutePath().toString());
			return;
		}		
	}
	
	public Substitutions(Path p,String file) {
		parseFile(p,file);
	}	
}
