package org.pale.simplechat.actions;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.pale.simplechat.Conversation;
import org.pale.simplechat.PatternParseException;
import org.pale.simplechat.BotConfigException;

public class InstructionStream {
	// limit on how many instructions can run
	private static final int LIMIT = 10000;
	
	private List<Instruction> insts;

	public InstructionStream(StreamTokenizer toks) throws IOException, BotConfigException, PatternParseException{
		insts = new InstructionCompiler(toks).insts;
	}
	
	// if reset is true, will clear lots of things - do this for a top-level call. Otherwise it's a user function call.
	public void run(Conversation c, boolean reset) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, ActionException {
		if(reset)c.reset(); // do this if it's a top level call
		int i=0; // instruction number (there might be jumps, see)
		c.exitflag = false;
		while(i<insts.size() && !c.exitflag){
			ActionLog.write("Running instruction at "+i+": "+insts.get(i).getClass().getSimpleName());

			// each instruction returns the next execution address's offset (usually 1!)
			i += insts.get(i).execute(c);
			if(c.totalinsts++ > LIMIT)throw new ActionException("Instruction limit exceeded");
		}	
	}
	
}
