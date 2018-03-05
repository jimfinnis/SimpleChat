package org.pale.simplechat.actions;

import java.util.List;

import org.pale.simplechat.Pair;

/**
 * Ugly, because the stack can contain anything and we can't have unions in Java.
 * @author white
 *
 */
public class Value {
	public enum Type {
		STRING,INT,DOUBLE,SUBPATS
	}
	int i;
	double d;
	String s;
	List<Pair> subpats;
	
	Type t;
	
	
	Value(String s){
		this.s = s;
		t = Type.STRING;
	}
	Value(int i){
		this.i = i;
		t = Type.INT;
	}
	Value(double d){
		this.d = d;
		t = Type.DOUBLE;
	}
	Value(List<Pair> p){
		this.subpats = p;
		t = Type.SUBPATS;
	}
	Value(boolean b){
		this.i = b?1:0;
		t = Type.INT;
	}
	
	String str(){
		switch(t){
		case INT:
			return Integer.toString(i);
		case DOUBLE:
			return Double.toString(d);
		case STRING:
			return s;
		case SUBPATS:
			return "SUBPATTERNS";
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
	boolean equals(Value b){
		if(t != b.t)return false;
		switch(t){
		case INT:return i==b.i;
		case DOUBLE:return d==b.d;
		case STRING:return s.equals(b.s);
		default:return false;
		}
	}
}
