package org.pale.simplechat.actions;

import java.util.ArrayList;
import java.util.List;

import org.pale.simplechat.Pair;

/**
 * Ugly, because the stack can contain anything and we can't have unions in Java.
 * @author white
 *
 */
public class Value {
	public enum Type {
		STRING,INT,DOUBLE,SUBPATS,LIST
	}
	int i;
	double d;
	String s;
	List<Pair> subpats;
	List<Value> list;
	
	Type t;
	
	private Value(){
		// private ctor for newList etc.
	}
	public static Value newList(){
		Value v = new Value();
		v.t = Type.LIST;
		v.list = new ArrayList<Value>();
		return v;
	}
	
	public Value(String s){
		this.s = s;
		t = Type.STRING;
	}
	public Value(int i){
		this.i = i;
		t = Type.INT;
	}
	public Value(double d){
		this.d = d;
		t = Type.DOUBLE;
	}
	public Value(List<Pair> p){
		this.subpats = p;
		t = Type.SUBPATS;
	}
	public Value(boolean b){
		this.i = b?1:0;
		t = Type.INT;
	}
	
	public String str(){
		switch(t){
		case INT:
			return Integer.toString(i);
		case DOUBLE:
			return Double.toString(d);
		case STRING:
			return s;
		case SUBPATS:
			return "SUBPATTERNS";
		case LIST:
			String out = "";
			for(Value v: list){
				out=out+v.str();
			}
			return out;
		default:return "??";
		}
	}
	
	int toInt() throws ActionException{
		switch(t){
		case INT:
			return i;
		case DOUBLE:
			return (int)d;
		case STRING:
			return Integer.parseInt(s);
		default:
			throw new ActionException("cannot convert value to int");
		}
	}
	double toDouble() throws ActionException{
		switch(t){
		case INT:
			return (double)i;
		case DOUBLE:
			return d;
		case STRING:
			return Double.parseDouble(s);
		default:
			throw new ActionException("cannot convert value to double");
		}
	}
	@Override
	public boolean equals(Object ob){
		if(this == ob)return true;
		if(!(ob instanceof Value))return false;
		Value b = (Value)ob;
				
		if(t != b.t)return false;
		switch(t){
		case INT:return i==b.i;
		case DOUBLE:return d==b.d;
		case STRING:return s.equals(b.s);
		case LIST:
			return list.equals(b.list);
		default:return false;
		}
	}
}
