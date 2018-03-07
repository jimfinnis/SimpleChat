package org.pale.simplechat.values;

import java.util.ArrayList;
import java.util.List;

import org.pale.simplechat.actions.Value;

public class ListValue extends Value {
	public List<Value> list;
	
	public ListValue(List<Value> l){
		this.list = l;
	}
	
	public ListValue(){
		this.list = new ArrayList<Value>();
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
}
