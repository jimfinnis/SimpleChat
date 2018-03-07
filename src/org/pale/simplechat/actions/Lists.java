package org.pale.simplechat.actions;

import org.pale.simplechat.Conversation;
import org.pale.simplechat.values.ListValue;

public class Lists  {

	public static class AppendInstruction extends Instruction {
		@Override
		int execute(Conversation c) throws ActionException {
			Value append = c.pop();
			Value lst = c.peek();
			if(!(lst instanceof ListValue))
				throw new ActionException("cannot append to non-list");
			((ListValue)lst).list.add(append); // should this be a clone??
			return 1;
		}

	}

	public static class NewListInstruction extends Instruction {
		@Override
		int execute(Conversation c) throws ActionException {
			c.push(new ListValue());
			return 1;
		}
	}
}
