package org.pale.simplechat.values;

import org.pale.simplechat.actions.BinopInstruction.Type;
import org.pale.simplechat.actions.Value;

public class NoneValue extends Value {
	
	static public NoneValue instance = new NoneValue();
	private NoneValue(){} // it's a singleton

	// binops on none always fail
	@Override
	public Value binop(Type t, Value snd) {
		return null;
	}
	@Override public String str(){
		return "<<NONE>>";
	}
	@Override public int toInt(){
		return 0;
	}

	
	@Override public boolean equals(Object ob){
		if(this == ob)return true;
		if(ob instanceof NoneValue)return true;
		return false;
	}
}
