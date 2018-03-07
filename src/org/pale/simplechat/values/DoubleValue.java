package org.pale.simplechat.values;

import org.pale.simplechat.actions.BinopInstruction.Type;
import org.pale.simplechat.actions.Value;

public class DoubleValue extends Value {
	double d;
	
	public DoubleValue(double d){
		this.d = d;
	}
	@Override public boolean equals(Object ob){
		if(this == ob)return true;
		if(!(ob instanceof DoubleValue))return false;
		DoubleValue b = (DoubleValue)ob;
		return b.d == d;
	}
	
	@Override public int toInt(){
		return (int)d;
	}
	
	@Override public double toDouble(){
		return d;
	}
	
	@Override public String str(){
		return Double.toString(d);
	}
	@Override
	public Value binop(Type t, Value snd) {
		Value r;
		switch(t){
		case ADD:
			if(snd instanceof IntValue)
				r = new DoubleValue(d+(double)((IntValue)snd).i);
			else if(snd instanceof DoubleValue)
				r = new DoubleValue(d+((DoubleValue)snd).d);
			else if(snd instanceof StringValue)
				r = new StringValue(str()+snd.str());
			else return null;
			break;
		case MUL:
			if(snd instanceof IntValue)
				r = new DoubleValue(d*(double)((IntValue)snd).i);
			else if(snd instanceof DoubleValue)
				r = new DoubleValue(d*((DoubleValue)snd).d);
			else return null;
			break;
		case DIV:
			if(snd instanceof IntValue)
				r = new DoubleValue(d/(double)((IntValue)snd).i);
			else if(snd instanceof DoubleValue)
				r = new DoubleValue(d/((DoubleValue)snd).d);
			else return null;
			break;
		case SUB:
			if(snd instanceof IntValue)
				r = new DoubleValue(d-(double)((IntValue)snd).i);
			else if(snd instanceof DoubleValue)
				r = new DoubleValue(d-((DoubleValue)snd).d);
			else return null;
			break;
		case MOD:
			if(snd instanceof IntValue)
				r = new DoubleValue(d%(double)((IntValue)snd).i);
			else if(snd instanceof DoubleValue)
				r = new DoubleValue(d%((DoubleValue)snd).d);
			else return null;
			break;
		case GT:
			if(snd instanceof IntValue)
				r = new IntValue(d>(double)((IntValue)snd).i);
			else if(snd instanceof DoubleValue)
				r = new IntValue(d>((DoubleValue)snd).d);
			else return null;
			break;
		case LT:
			if(snd instanceof IntValue)
				r = new IntValue(d<(double)((IntValue)snd).i);
			else if(snd instanceof DoubleValue)
				r = new IntValue(d<((DoubleValue)snd).d);
			else return null;
			break;
		case GE:
			if(snd instanceof IntValue)
				r = new IntValue(d>=(double)((IntValue)snd).i);
			else if(snd instanceof DoubleValue)
				r = new IntValue(d>=((DoubleValue)snd).d);
			else return null;
			break;
		case LE:
			if(snd instanceof IntValue)
				r = new IntValue(d<=(double)((IntValue)snd).i);
			else if(snd instanceof DoubleValue)
				r = new IntValue(d<=((DoubleValue)snd).d);
			else return null;
			break;
		default: return null;
		}
		return r;
	}

}
