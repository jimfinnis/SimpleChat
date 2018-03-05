package org.pale.simplechat.actions;

import org.pale.simplechat.Conversation;

public class BinopInstruction extends Instruction {

	public enum Type {
		ADD, SUB, DIV, MUL, MOD, EQUAL, NEQUAL,
		GT, LT, GE, LE
	}

	private Type type;
	
	public BinopInstruction(Type op){
		type = op;
	}
	
	@Override
	public int execute(Conversation c) throws ActionException {
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
		case EQUAL:
			c.push(new Value(a.equals(b)));
			break;
		case NEQUAL:
			c.push(new Value(!a.equals(b)));
			break;
		case GT:
			if(a.t == Value.Type.DOUBLE || b.t == Value.Type.DOUBLE){
				c.push(new Value(a.toDouble()>b.toDouble()));
			} else if(a.t == Value.Type.INT || b.t == Value.Type.INT){
				c.push(new Value(a.toInt()>b.toInt()));
			} else throw new ActionException("bad operands for %");
			break;
		case LT:
			if(a.t == Value.Type.DOUBLE || b.t == Value.Type.DOUBLE){
				c.push(new Value(a.toDouble()<b.toDouble()));
			} else if(a.t == Value.Type.INT || b.t == Value.Type.INT){
				c.push(new Value(a.toInt()<b.toInt()));
			} else throw new ActionException("bad operands for %");
			break;
		case GE:
			if(a.t == Value.Type.DOUBLE || b.t == Value.Type.DOUBLE){
				c.push(new Value(a.toDouble()>=b.toDouble()));
			} else if(a.t == Value.Type.INT || b.t == Value.Type.INT){
				c.push(new Value(a.toInt()>=b.toInt()));
			} else throw new ActionException("bad operands for %");
			break;
		case LE:
			if(a.t == Value.Type.DOUBLE || b.t == Value.Type.DOUBLE){
				c.push(new Value(a.toDouble()<=b.toDouble()));
			} else if(a.t == Value.Type.INT || b.t == Value.Type.INT){
				c.push(new Value(a.toInt()<=b.toInt()));
			} else throw new ActionException("bad operands for %");
			break;
		}
		return 1;
	}
}
