package org.pale.simplechat.values;

import org.pale.simplechat.actions.Value;
import org.pale.simplechat.actions.BinopInstruction.Type;

public class IntValue extends Value{
	int i;
	
	public IntValue(int i){
		this.i = i;
	}
	
	public IntValue(boolean b){
		this.i = b ? 1 : 0; // construct a "boolean" value as an int
	}
	
	
	@Override public boolean equals(Object ob){
		if(this == ob)return true;
		if(!(ob instanceof IntValue))return false;
		IntValue b = (IntValue)ob;
		return b.i == i;
	}
	
	@Override public int toInt(){
		return i;
	}
	
	@Override public double toDouble(){
		return (double)i;
	}
	
	@Override public String str(){
		return Integer.toString(i);
	}
	@Override
	public Value binop(Type t, Value snd) {
		Value r;
		switch(t){
		case ADD:
			if(snd instanceof IntValue)
				r = new IntValue(i+((IntValue)snd).i);
			else if(snd instanceof DoubleValue)
				r = new DoubleValue((double)i+((DoubleValue)snd).d);
			else if(snd instanceof StringValue)
				r = new StringValue(str()+snd.str());
			else return null;
			break;
		case MUL:
			if(snd instanceof IntValue)
				r = new IntValue(i*((IntValue)snd).i);
			else if(snd instanceof DoubleValue)
				r = new DoubleValue((double)i*((DoubleValue)snd).d);
			else return null;
			break;
		case DIV:
			if(snd instanceof IntValue)
				r = new IntValue(i/((IntValue)snd).i);
			else if(snd instanceof DoubleValue)
				r = new DoubleValue((double)i/((DoubleValue)snd).d);
			else return null;
			break;
		case SUB:
			if(snd instanceof IntValue)
				r = new IntValue(i-((IntValue)snd).i);
			else if(snd instanceof DoubleValue)
				r = new DoubleValue((double)i-((DoubleValue)snd).d);
			else return null;
			break;
		case MOD:
			if(snd instanceof IntValue)
				r = new IntValue(i%((IntValue)snd).i);
			else if(snd instanceof DoubleValue)
				r = new DoubleValue((double)i%((DoubleValue)snd).d);
			else return null;
			break;
		case GT:
			if(snd instanceof IntValue)
				r = new IntValue(i>((IntValue)snd).i);
			else if(snd instanceof DoubleValue)
				r = new IntValue((double)i>((DoubleValue)snd).d);
			else return null;
			break;
		case LT:
			if(snd instanceof IntValue)
				r = new IntValue(i<((IntValue)snd).i);
			else if(snd instanceof DoubleValue)
				r = new IntValue((double)i<((DoubleValue)snd).d);
			else return null;
			break;
		case GE:
			if(snd instanceof IntValue)
				r = new IntValue(i>=((IntValue)snd).i);
			else if(snd instanceof DoubleValue)
				r = new IntValue((double)i>=((DoubleValue)snd).d);
			else return null;
			break;
		case LE:
			if(snd instanceof IntValue)
				r = new IntValue(i<=((IntValue)snd).i);
			else if(snd instanceof DoubleValue)
				r = new IntValue((double)i<=((DoubleValue)snd).d);
			else return null;
			break;
		case AND:
			if(snd instanceof NoneValue)
				r = new IntValue(0);
			else if(snd instanceof IntValue)
				r = new IntValue(i!=0 && ((IntValue)snd).i!=0);
			else return null;
			break;
		case OR:
			if(snd instanceof NoneValue)
				r = new IntValue(0);
			else if(snd instanceof IntValue)
				r = new IntValue(i!=0 || ((IntValue)snd).i!=0);
			else return null;
			break;
		default: return null;
		}
		return r;
	}
	
	public Value neg() {
		return new DoubleValue(-i);
	}

}
