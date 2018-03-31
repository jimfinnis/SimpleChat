package org.pale.simplechat.values;

import java.util.HashMap;
import java.util.Map;

import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.BinopInstruction.Type;
import org.pale.simplechat.actions.MapLoopIterator;
import org.pale.simplechat.actions.Runtime.LoopIterator;
import org.pale.simplechat.actions.Value;

public class MapValue extends Value {
	public Map<String,Value> map;
	
	public MapValue(Map<String,Value> map){
		this.map = map;
	}
	
	public MapValue(){
		map = new HashMap<String,Value>();
	}
	
	@Override
	public Value get(Value k) throws ActionException {
		Value v = map.get(k.str());
		if(v==null)
			return NoneValue.instance;
		else
			return v;
	}
	@Override
	public void set(Value k,Value v) throws ActionException {
		String sk = k.str();
		if(v == NoneValue.instance)
			map.remove(sk);
		else
			map.put(sk, v);
	}
	@Override
	public boolean containsKey(Value k) throws ActionException {
		return map.containsKey(k.str());
	}
	
	@Override
	public Value binop(Type t, Value snd) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override public boolean equals(Object ob){
		if(this == ob)return true;
		if(!(ob instanceof MapValue))return false;
		MapValue b = (MapValue)ob;
		return b.map.equals(map);
	}

	@Override public String str(){
		StringBuilder sb = new StringBuilder();
		boolean first=true;
		for(Map.Entry<String, Value> e: map.entrySet()){
			if(first)
				first = false;
			else
				sb.append(",");
			sb.append(e.getKey());
			sb.append("=");
			sb.append(e.getValue().str());
		}
		return sb.toString();
	}

	@Override
	public int size() throws ActionException {
		return map.size();
	}
	
	@Override
	public LoopIterator makeIterator() throws ActionException {
		return new MapLoopIterator(map);
	}



}
