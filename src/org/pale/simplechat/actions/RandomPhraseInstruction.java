package org.pale.simplechat.actions;

import org.pale.simplechat.Conversation;
import org.pale.simplechat.PhraseList;
import org.pale.simplechat.values.StringValue;

public class RandomPhraseInstruction extends Instruction {
	private PhraseList l;
	
	public RandomPhraseInstruction(PhraseList l){
		this.l = l;
	}
	@Override
	int execute(Conversation c) throws ActionException {
		c.push(new StringValue(l.random()));
		return 1;
	}

}
