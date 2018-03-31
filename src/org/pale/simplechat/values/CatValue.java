package org.pale.simplechat.values;

import org.pale.simplechat.Category;
import org.pale.simplechat.actions.BinopInstruction.Type;
import org.pale.simplechat.actions.Value;

public class CatValue extends Value {
	public Category c;
	private String name;
	
	public CatValue(String name,Category c) {
		this.name = name;
		this.c = c;
	}
	
	@Override
	public String str() {
		return name;
	}

	@Override
	public Value binop(Type t, Value snd) {
		return null;
	}
}
