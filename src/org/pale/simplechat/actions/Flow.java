package org.pale.simplechat.actions;

import org.pale.simplechat.Conversation;

/**
 * This class is just a container - I use it to keep the flow control instructions all in one place.
 * @author white
 *
 */
public class Flow {
	public static class JumpInstruction extends Instruction {
		int offset;
		
		@Override
		public int execute(Conversation c)  throws ActionException {
			return offset;
		}
		@Override
		public void setJump(int offset){
			this.offset=offset;
		}
	}

	public static class IfInstruction extends JumpInstruction {
		@Override
		public int execute(Conversation c) throws ActionException  {
			int condition = c.pop().toInt();
			if(condition!=0)
				return 1;
			else
				return offset;
		}
		
	}
}
