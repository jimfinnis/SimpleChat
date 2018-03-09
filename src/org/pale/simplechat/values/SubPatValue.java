package org.pale.simplechat.values;

import java.util.List;

import org.pale.simplechat.Pair;
import org.pale.simplechat.actions.Value;
import org.pale.simplechat.actions.BinopInstruction.Type;

public class SubPatValue extends Value {
	public List<Pair> subpats;
	
	public SubPatValue(List<Pair> sb){
		this.subpats = sb;
	}
	@Override public boolean equals(Object ob){
		return false;
	}
	
	@Override public String str(){
		return "<SUBPAT VALUE> (did you forget 'next'?)";
	}
	@Override
	public Value binop(Type t, Value snd) {
		return null;
	}


}
