package org.pale.simplechat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pale.simplechat.actions.InstructionCompiler;
import org.pale.simplechat.actions.InstructionStream;

/** a Topic is a list of patterns in the order they were read in a file,
 *  with their associated actions.
 * @author white
 *
 */
public class Topic {
	/**
	 * List of patterns in order they were added
	 */
	
	List<Pair> pairList = new ArrayList<Pair>();
	
	/**
	 * Map of pairs which have names
	 */
	Map<String,Pair> pairMap = new HashMap<String,Pair>();
	

	String name;
	StreamTokenizer tok;

	private double getNextDouble() throws BotConfigException, IOException{
		if(tok.nextToken()!=StreamTokenizer.TT_NUMBER)
			throw new BotConfigException("Expected a number");
		return (double)tok.nval;
	}

	private String getNextString() throws BotConfigException, IOException{
		int tt = tok.nextToken();
		if(tt != '\'' && tt != '\"')
			throw new BotConfigException("Expected a string");
		return tok.sval;
	}

	private String getNextIdent() throws BotConfigException, IOException{
		if(tok.nextToken()!=StreamTokenizer.TT_WORD)
			throw new BotConfigException("Expected an identifier");
		return tok.sval;
	}
	/**
	 * Load the topic data, and add it to the given bot. May recurse, in that topics
	 * may trigger the loading of other topics.
	 * @param path
	 * @param bot
	 * @throws PatternParseException 
	 * @throws IOException 
	 * @throws BotConfigException 
	 */
	public Topic(String name,Path f) throws BotConfigException {
		try {
			this.name = name;
			BufferedReader r = Files.newBufferedReader(f); 
			tok = new StreamTokenizer(r);
			tok.commentChar('#');
			tok.ordinaryChar('/');
			
			// we got the name of the topic, so parse a list of pattern/action pairs, or possibly other topic includes
			for(;;){
				int t = tok.nextToken();
				if(t == StreamTokenizer.TT_EOF)break;
				else if(t == ':')InstructionCompiler.parseFunction(tok);
				else if(t == '+'){
					// pattern line is +"pattern" .. OR +name "pattern"
					String pname,pstring;
					int tt = tok.nextToken();
					if(tt==StreamTokenizer.TT_WORD){
						// pattern has a name
						pname = tok.sval;
						pstring = getNextString();
					} else if(tt=='\''||tt=='\"'){
						// pattern is anonymous
						pname = null;
						pstring = tok.sval;
					} else
						throw new BotConfigException("badly formed pattern definition");
					Pattern pat = new Pattern(pname,pstring);
					InstructionStream act = new InstructionStream(tok);
					Pair pair = new Pair(pat,act);
					if(pname!=null)
						pairMap.put(pname,pair);
					pairList.add(pair);
					Logger.log("pattern/action pair parsed");
				} else
					throw new BotConfigException("badly formed topic file, expected '+'");
			}
		} catch (BotConfigException e){
			Logger.log("syntax error in topic file "+f.toString()+" : "+e.getMessage());
			throw e; // log and rethrow
		} catch (IOException e) {
			String s ="IO error in topic file "+f.toString();
			Logger.log(s);
			throw new BotConfigException(s);
		} catch(PatternParseException e){
			String s = "Pattern parse error in topic file "+f.toString()+" : "+e.getMessage();
			Logger.log(s);
			throw new BotConfigException(s);
		}

		tok = null; // discard tokeniser when done
	}
}
