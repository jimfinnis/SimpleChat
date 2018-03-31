package org.pale.simplechat.values;

import org.pale.simplechat.actions.BinopInstruction.Type;
import org.pale.simplechat.actions.Value;

public class NoneValue extends Value {
	
	static public NoneValue instance = new NoneValue();
	private NoneValue(){} // it's a singleton

	@Override public String str(){
		return "<<NONE>>";
	}
	@Override public int toInt(){
		return 0;
	}

	// only valid binop for none is logical; always returns false.
	@Override
	public Value binop(Type t, Value snd) {
		if(!(snd instanceof IntValue))return null;
		IntValue i = (IntValue)snd;
		switch(t){
		case AND:
			return new IntValue(0);
		case OR:
			return i;
		default:
			return null;
		}
	}
	

	
	@Override public boolean equals(Object ob){
		if(this == ob)return true;
		if(ob instanceof NoneValue)return true;
		return false;
	}
	
	@Override public boolean containsKey(Value v){
		return false;
	}


}
