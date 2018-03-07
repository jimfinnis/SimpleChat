package org.pale.simplechat.values;

import java.util.ArrayList;
import java.util.List;

import org.pale.simplechat.actions.Value;
import org.pale.simplechat.actions.BinopInstruction.Type;

public class ListValue extends Value {
	public List<Value> list;

	public ListValue(List<Value> l){
		this.list = l;
	}

	public ListValue(){
		this.list = new ArrayList<Value>();
	}
	
	// sort-of copy ctor.
	public ListValue copy(List<Value> l){
		ListValue out = new ListValue();
		for(Value v: l){
			out.list.add(v);
		}
		return out;
	}

	@Override public boolean equals(Object ob){
		if(this == ob)return true;
		if(!(ob instanceof ListValue))return false;
		ListValue b = (ListValue)ob;
		return b.list.equals(list);
	}

	@Override public String str(){
		StringBuilder sb = new StringBuilder();
		boolean first=true;
		for(Value v: list){
			if(first)
				first = false;
			else
				sb.append(",");
			sb.append(v.str());

		}
		return sb.toString();
	}
	@Override
	public Value binop(Type t, Value snd) {
		switch(t){
		case ADD:
			// this only works for list joining; to add items to a list
			// use the , operator.
			if(snd instanceof ListValue){
				ListValue out = copy(this.list);
				List<Value> l2 = ((ListValue)snd).list;
				for(Value v: l2)
					out.list.add(v);
				return out;
			}
		default:
			return null;
		}
	}
}
