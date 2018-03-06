package org.pale.simplechat.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.pale.simplechat.Conversation;
import org.pale.simplechat.Logger;

// this is a list of user functions
class Function {
	String name;
	String[] args;	// argument names
	String[] locals;	// local names
	InstructionStream insts; // the function's instructions
	
	// by this point we should have parsed the function introducer and arguments
	// and be ready to parse the instruction stream.
	// Takes the argument and local names. Arguments will
	// be popped off the stack, locals will just be created in the convo.
	// This doesn't set the actual code - that's done afterwards. We define the func and add
	// it to the internal map before compiling it, so we permit recursion.
	Function(String name,String[] args,String[] locals) {
		this.name = name;
		this.args=args;
		this.locals=locals;
		this.insts = null;
		Logger.log("New function : "+name);
	}
	
	void setInsts(InstructionStream i){
		this.insts = i;
	}
	
	void run(Conversation c) throws ActionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		// we save the OLD locals (which will may be null)
		Map<String,Value> oldLocals = c.funcVars;
		// we pop off the arguments into new local variables of the same name. It's a good job
		// we're not too worried about speed, isn't it?
		Map<String,Value> llist;
		if(args!=null || locals!=null)
			llist = new HashMap<String,Value>();
		else
			llist=null;
		if(args!=null){
			for(int i=args.length-1;i>=0;i--){
				llist.put(args[i], c.pop());
			}
		}
		if(locals!=null){
			for(String n : locals){
				llist.put(n,new Value(0)); // all locals initialized to 0
			}
		}
		// we set the conversation's local vars to this
		c.funcVars = llist;
		
		// we run the function
		insts.run(c,false);
		
		// and we restore the locals
		c.funcVars = oldLocals;
	}
}