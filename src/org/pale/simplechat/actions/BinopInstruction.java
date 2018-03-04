package org.pale.simplechat.actions;

import java.lang.reflect.InvocationTargetException;

import org.pale.simplechat.Conversation;

public class BinopInstruction implements Instruction {

	public enum Type {
		ADD, SUB, DIV, MUL, MOD
	}

	private Type type;
	
	public BinopInstruction(Type op){
		type = op;
	}
	
	@Override
	public void execute(Conversation c) throws ActionException {
		Value b = c.pop();
		Value a = c.pop();
		
		switch(type){
		case ADD: // support string adding
			if(a.t == Value.Type.STRING || b.t == Value.Type.STRING){
				String s1 = a.str();
				String s2 = b.str();
				c.push(new Value(s1+s2));
			} else if(a.t == Value.Type.DOUBLE || b.t == Value.Type.DOUBLE){
				c.push(new Value(a.toDouble()+b.toDouble()));
			} else if(a.t == Value.Type.INT || b.t == Value.Type.INT){
				c.push(new Value(a.toInt()+b.toInt()));
			} else throw new ActionException("bad operands for +");
			break;
		case MUL: // string*num = string repeat
			if(a.t == Value.Type.STRING) {
				int n = b.toInt();
				c.push(new Value(new String(new char[n]).replace("\0", a.s)));
			} else if(a.t == Value.Type.DOUBLE || b.t == Value.Type.DOUBLE){
				c.push(new Value(a.toDouble()*b.toDouble()));
			} else if(a.t == Value.Type.INT || b.t == Value.Type.INT){
				c.push(new Value(a.toInt()*b.toInt()));
			} else throw new ActionException("bad operands for *");
			break;
		case DIV:
			if(a.t == Value.Type.DOUBLE || b.t == Value.Type.DOUBLE){
				c.push(new Value(a.toDouble()/b.toDouble()));
			} else if(a.t == Value.Type.INT || b.t == Value.Type.INT){
				c.push(new Value(a.toInt()/b.toInt()));
			} else throw new ActionException("bad operands for /");
			break;
		case SUB:
			if(a.t == Value.Type.DOUBLE || b.t == Value.Type.DOUBLE){
				c.push(new Value(a.toDouble()-b.toDouble()));
			} else if(a.t == Value.Type.INT || b.t == Value.Type.INT){
				c.push(new Value(a.toInt()-b.toInt()));
			} else throw new ActionException("bad operands for -");
			break;
		case MOD:
			if(a.t == Value.Type.DOUBLE || b.t == Value.Type.DOUBLE){
				c.push(new Value(a.toDouble()%b.toDouble()));
			} else if(a.t == Value.Type.INT || b.t == Value.Type.INT){
				c.push(new Value(a.toInt()%b.toInt()));
			} else throw new ActionException("bad operands for %");
			break;
		}
	}
}
