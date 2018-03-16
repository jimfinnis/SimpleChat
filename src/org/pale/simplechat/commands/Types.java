package org.pale.simplechat.commands;

import org.pale.simplechat.Conversation;
import org.pale.simplechat.Logger;
import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.Cmd;
import org.pale.simplechat.actions.InstructionCompiler;
import org.pale.simplechat.values.DoubleValue;
import org.pale.simplechat.values.IntValue;
import org.pale.simplechat.values.NoneValue;
import org.pale.simplechat.values.RangeValue;
import org.pale.simplechat.values.StringValue;

public class Types {


	/*
	 * Type conversion
	 */
	@Cmd public static void str(Conversation c) throws ActionException {
		c.push(new StringValue(c.pop().str()));
	}

	@Cmd (name="int") public static void toint(Conversation c) throws ActionException {
		c.push(new IntValue(c.pop().toInt()));
	}

	@Cmd (name="double") public static void todouble(Conversation c) throws ActionException {
		c.push(new DoubleValue(c.pop().toDouble()));
	}

	/*
	 * None handling
	 */
	@Cmd public static void isnone(Conversation c) throws ActionException {
		c.push(new IntValue((c.pop().equals(NoneValue.instance))));
	}

	@Cmd public static void none(Conversation c) throws ActionException {
		c.push(NoneValue.instance);
	}
	
	/*
	 * Range constructor
	 */
	@Cmd public static void range(Conversation c) throws ActionException {
		int end = c.pop().toInt();
		int start = c.pop().toInt();
		c.push(new RangeValue(start,end));
	}


}
