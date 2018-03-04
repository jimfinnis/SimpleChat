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

import org.pale.simplechat.actions.Action;

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

	private double getNextDouble() throws TopicSyntaxException, IOException{
		if(tok.nextToken()!=StreamTokenizer.TT_NUMBER)
			throw new TopicSyntaxException("Expected a number");
		return (double)tok.nval;
	}

	private String getNextString() throws TopicSyntaxException, IOException{
		int tt = tok.nextToken();
		if(tt != '\'' && tt != '\"')
			throw new TopicSyntaxException("Expected a string");
		return tok.sval;
	}

	private String getNextIdent() throws TopicSyntaxException, IOException{
		if(tok.nextToken()!=StreamTokenizer.TT_WORD)
			throw new TopicSyntaxException("Expected an identifier");
		return tok.sval;
	}
	/**
	 * Load the topic data, and add it to the given bot. May recurse, in that topics
	 * may trigger the loading of other topics.
	 * @param path
	 * @param bot
	 * @throws PatternParseException 
	 * @throws IOException 
	 * @throws TopicSyntaxException 
	 */
	public Topic(Path path,String file, Bot bot) {
		Path f = null;
		try {
			f = path.resolve(file);
			BufferedReader r = Files.newBufferedReader(f); 
			tok = new StreamTokenizer(r);
			tok.commentChar('#');
			tok.ordinaryChar('/');
			
			// top level parsing
			if(!getNextIdent().equals("name"))throw new TopicSyntaxException("'name' must be first token in topic");
			name = getNextIdent();
			double priority = 1;

			// we got the name of the topic, so parse a list of pattern/action pairs, or possibly other topic includes
			for(;;){
				int t = tok.nextToken();
				if(t == StreamTokenizer.TT_EOF)break;
				else if(t == StreamTokenizer.TT_WORD){
					if(tok.sval.equals("topic"))new Topic(path,getNextString(),bot);
					else if(tok.sval.equals("priority"))priority = getNextDouble();
				}
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
						throw new TopicSyntaxException("badly formed pattern definition");
					Pattern pat = new Pattern(pname,pstring);
					Action act = new Action(tok);
					Pair pair = new Pair(pat,act);
					if(pname!=null)
						pairMap.put(pname,pair);
					pairList.add(pair);
					Logger.log("pattern/action pair parsed");
				}
			}
			// add this topic to the bot.
			bot.addTopic(name,this,priority);
			
		
		} catch (TopicSyntaxException e){
			Logger.log("syntax error in topic file "+f.toString()+" : "+e.getMessage());
		} catch (IOException e) {
			if(f==null)
				Logger.log("IO error in topic dir "+path.toString());
			else
				Logger.log("IO error in topic file "+f.toString());
		} catch(PatternParseException e){
			Logger.log("Pattern parse error in topic file "+f.toString()+" : "+e.getMessage());
		}

		tok = null; // discard tokeniser when done

	}
}
