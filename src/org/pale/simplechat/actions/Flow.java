package org.pale.simplechat.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.pale.simplechat.Conversation;
import org.pale.simplechat.actions.Runtime.LoopIterator;

/**
 * This class is just a container - I use it to keep the flow control instructions all in one place.
 * @author white
 *
 */
public class Flow {
	public static class LoopGetInstruction extends Instruction {
		private int n;
		
		public LoopGetInstruction(int n) {
			this.n = n;
		}
		@Override
		int execute(Conversation c) throws ActionException {
			// we're going to have to do a weird peek into the stack here.
			LoopIterator iter = c.iterStackPeek(n);
			c.push(iter.current());
			return 1;
		}
	}

	public static class IterLoopStartInstruction extends Instruction {
		@Override
		int execute(Conversation c) throws ActionException {
			// get the iterable and make an iterator, and put that onto the iterstack.
			Value v = c.pop();
			LoopIterator iter = v.makeIterator();
			c.iterStack.push(iter);
			return 1;
		}
	}
	
	public static class IterLoopLeaveIfDone extends LeaveInstruction {
		@Override
		public int execute(Conversation c) throws ActionException  {
			LoopIterator iter = c.iterStack.peek();
			if(iter.hasNext()){
				iter.next();
				// get the next thingy, advancing the iterator, and move on..
				return 1;
			} else {
				// loop is done, pop the iterator and jump out
				c.iterStack.pop();
				return offset;
			}
		}
	}


	public static class JumpInstruction extends Instruction {
		// how far to jump.
		// 0 is used as a default to be fixed up
		int offset; 
		// but if this is set, it's a case jump to be fixed up. Different to permit syntax checking.
		public boolean isCaseJump=false;
		
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
	
	public static class RandBlockInstruction extends Instruction {
		public List<Integer> addresses = new ArrayList<Integer>();
		@Override
		int execute(Conversation c) throws ActionException {
			// If there are n clauses in the random block, there will be n-1 offsets (there's always offset 0, you see).
			int n = ThreadLocalRandom.current().nextInt(addresses.size()+1);
			if(n==0)
				return 1;
			else
				return addresses.get(n-1);
		}
		
	}
	
}
