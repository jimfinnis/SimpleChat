package org.pale.simplechat.actions;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;

import org.pale.simplechat.Bot;
import org.pale.simplechat.Conversation;
import org.pale.simplechat.ParserError;
import org.pale.simplechat.Tokenizer;
import org.pale.simplechat.values.StringValue;

// this instruction contains code to stack a string, but to
// also resolve any interpolated code within it
public class StringInstruction extends Instruction {
	String[] textElements;
	InstructionStream[] codeElements;
	
	public StringInstruction(Bot bot, String sval) throws IOException, ParserError {
		List<String> texels = new ArrayList<String>();
		List<InstructionStream> codels = new ArrayList<InstructionStream>();
		
		// easiest thing to do is just run through the string
		CharacterIterator i = new StringCharacterIterator(sval);
		StringBuilder sbtex = new StringBuilder(); 
		while(i.current()!=CharacterIterator.DONE){
			if(i.current()=='$'){
				if(i.next()=='{'){
					// make new texel and discard the previous chars
					texels.add(sbtex.toString());
					sbtex = new StringBuilder();
					// get string
					StringBuilder sb = new StringBuilder();
					for(;;){
						char c = i.next();
						if(c==CharacterIterator.DONE || c=='}')break;
						sb.append(c);
					}
					i.next();
					// we got a block of code, compile it.
					codels.add(new InstructionStream(bot,new Tokenizer(new StringReader(sb.toString()))));
				} else {
					sbtex.append('$');
					sbtex.append(i.current());
					i.next();
				}
			} else {
				sbtex.append(i.current());
				i.next();
			}
		}
		// add trailing text element
		texels.add(sbtex.toString());
		
		textElements = texels.toArray(new String[texels.size()]);
		codeElements = codels.toArray(new InstructionStream[codels.size()]);
	}

	
	@Override
	int execute(Conversation c) throws ActionException {
		String r;
		if(textElements.length==1)
			r=textElements[0];
		else {
			StringBuilder sb = new StringBuilder();
			for(int i=0;i<textElements.length;i++){
				sb.append(textElements[i]);
				if(i!=textElements.length-1){
					// if we're not on the last element, process the next code element.
					try {
						codeElements[i].run(c, false);
						sb.append(c.getResult());
					} catch (IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e) {
						throw new ActionException("problem with command in embedded code: "+e.getMessage());
					}
				}
			}
			r = sb.toString();
		}
		c.push(new StringValue(r));
		return 1;
	}

}
