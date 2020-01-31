package org.pale.simplechat.values;

import java.util.ArrayList;
import java.util.List;

import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.BinopInstruction.Type;
import org.pale.simplechat.actions.ListLoopIterator;
import org.pale.simplechat.actions.Runtime.LoopIterator;
import org.pale.simplechat.actions.Value;

public class ListValue extends Value {
	public List<Value> list;

	public ListValue(List<Value> l){
		this.list = l;
	}

	public ListValue(){
		this.list = new ArrayList<Value>();
	}
	
	@Override
	public Value get(Value k) throws ActionException {
		int i = k.toInt();
		
		if(i<0 || i>=list.size())
			return NoneValue.instance;
		else
			return list.get(i);
	}
	
	@Override
	public void set(Value k,Value v) throws ActionException {
		int i = k.toInt();
		if(i<0 || i>=list.size())
			throw new ActionException("set out of range in list");
		else
			list.set(i, v);
	}
	@Override
	public boolean containsKey(Value k) throws ActionException {
		return list.contains(k);
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
		sb.append('[');
		boolean first=true;
		for(Value v: list){
			if(first)
				first = false;
			else
				sb.append(",");
			sb.append(v.str());

		}
		sb.append(']');
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
	
	@Override
	public int size() throws ActionException {
		return list.size();
	}


	@Override
	public LoopIterator makeIterator() throws ActionException {
		return new ListLoopIterator(list);
	}
}
