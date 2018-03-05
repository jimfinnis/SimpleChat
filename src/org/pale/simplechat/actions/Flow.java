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
		
		JumpInstruction(){ // default ctor for jumps which get fixed up later
			offset=0;
		}
		
		JumpInstruction(int n){ // ctor for jumps with a known offset
			offset = n;
		}
		
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
	
	public static class LoopStartInstruction extends Instruction {
		@Override
		public int execute(Conversation c) throws ActionException  {
			// push a null iterator onto the loop iterator stack, as
			// this is not an iterator loop
			c.iterStack.push(null);
			return 1;
		}
	
	}
	public static class LeaveInstruction extends JumpInstruction {
		@Override
		public int execute(Conversation c) throws ActionException  {
			// pop the iterator stack
			c.iterStack.pop();
			// and jump
			return offset;
		}
	}
	
	public static class IfLeaveInstruction extends LeaveInstruction {
		@Override
		public int execute(Conversation c) throws ActionException  {
			int condition = c.pop().toInt();
			if(condition==0)
				return 1;
			else {
				c.iterStack.pop();
				return offset;
			}
		}
	}
	
	public static class StopInstruction extends Instruction {
		@Override
		int execute(Conversation c) throws ActionException {
			c.exitflag = true;
			return 0;
		}
	}
}
