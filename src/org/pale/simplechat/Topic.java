package org.pale.simplechat;

import java.io.IOException;
import java.io.StreamTokenizer;
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
	

	public String name;
	Tokenizer tok;

	/**
	 * Load the topic data, and add it to the given bot. May recurse, in that topics
	 * may trigger the loading of other topics.
	 * @param path
	 * @param bot
	 * @throws PatternParseException 
	 * @throws IOException 
	 * @throws BotConfigException 
	 */
	public Topic(Bot bot,String name,Path f) throws BotConfigException {
		try {
			this.name = name;
			tok = new Tokenizer(f);

			Logger.log(Logger.CONFIG, "+++++++++++++++++++++++++++++++++ Parsing topic file for "+bot.getName()+": "+name);
			
			// we got the name of the topic, so parse a list of pattern/action pairs, or possibly other topic includes
			for(;;){
				int t = tok.nextToken();
				if(t == StreamTokenizer.TT_EOF)break;
				else if(t == ':')InstructionCompiler.parseNamedFunction(bot,tok);
				else if(t == '~')Category.parseCat(bot, tok);
				else if(t == '+'){
					// pattern line is + .. OR +/name ...
					String pname=null;
					int tt = tok.nextToken();
					if(tt=='/'){
						// pattern has a name
						if(tok.nextToken()!=StreamTokenizer.TT_WORD)
							throw new ParserError("expected a pattern name after '+/'");
						pname = tok.sval;
					} else tok.pushBack();
					Pattern pat = new Pattern(bot,pname,tok);
					InstructionStream act = new InstructionStream(bot,tok);
					Pair pair = new Pair(pat,act);
					if(pname!=null)
						pairMap.put(pname,pair);
					pairList.add(pair);
					Logger.log(Logger.CONFIG,"pattern/action pair parsed");
				} else
					throw new BotConfigException(f,tok,"badly formed topic file, expected '+'");
			}
		} catch (BotConfigException e){
			e.printStackTrace();
			Logger.log(Logger.FATAL,"syntax error in topic file "+e.getMessage());
			throw e; // log and rethrow
		} catch (IOException e) {
			e.printStackTrace();
			throw new BotConfigException(f,"Cannot read topic file");
		} catch (ParserError e) {
			Logger.log(Logger.FATAL,"syntax error in topic file - "+e.getMessage());
			throw new BotConfigException(f,tok,e.getMessage());
		}

		Logger.log(Logger.CONFIG, "--------------------------------- topic file done, "+bot.getName()+":"+name);
		tok = null; // discard tokeniser when done
	}
}
