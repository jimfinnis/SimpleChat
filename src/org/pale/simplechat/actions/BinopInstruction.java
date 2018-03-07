package org.pale.simplechat.actions;

import org.pale.simplechat.Conversation;
import org.pale.simplechat.values.IntValue;
import org.pale.simplechat.values.StringValue;

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
		Value r;

		if(type == Type.ADD && a instanceof StringValue || b instanceof StringValue ){
			r = new StringValue(a.str()+b.str());
		} else {

			switch(type){
			case EQUAL:r = new IntValue(a.equals(b));break;
			case NEQUAL:r = new IntValue(!a.equals(b));break;
			default:
				r = a.binop(type, b);
				if(r==null)
					throw new ActionException("binop "+type.name()+" is invalid for arguments "+
							a.getClass().getSimpleName()+","+b.getClass().getSimpleName());
				break;
			}
		}
		c.push(r);
		return 1;
	}
}

