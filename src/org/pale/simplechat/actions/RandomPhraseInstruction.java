package org.pale.simplechat.actions;

import org.pale.simplechat.Conversation;
import org.pale.simplechat.ParserError;
import org.pale.simplechat.PhraseList;
import org.pale.simplechat.values.StringValue;

public class RandomPhraseInstruction extends Instruction {
	private String n;
	
	public RandomPhraseInstruction(String n){
		this.n = n;
	}
	@Override
	int execute(Conversation c) throws ActionException {
		PhraseList l = c.instance.bot.getPhraseList(n);
		if(l==null)
			throw new ActionException("unknown phrase list: "+n);

		c.push(new StringValue(l.random()));
		return 1;
	}

}
