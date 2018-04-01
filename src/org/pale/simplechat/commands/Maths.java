package org.pale.simplechat.commands;

import java.util.concurrent.ThreadLocalRandom;

import org.pale.simplechat.Conversation;
import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.Cmd;
import org.pale.simplechat.actions.Value;
import org.pale.simplechat.values.DoubleValue;
import org.pale.simplechat.values.IntValue;

public class Maths {
	@Cmd public static void rand(Conversation c) throws ActionException {
		// (int --)
		c.push(new IntValue(ThreadLocalRandom.current().nextInt(c.pop().toInt())));

	}

	@Cmd public static void not(Conversation c) throws ActionException {
		int n = c.pop().toInt();
		c.push(new IntValue(n==0 ? 1 : 0));
	}
	
	@Cmd public static void neg(Conversation c) throws ActionException {
		Value v = c.pop();
		c.push(v.neg());
	}
	
	@Cmd public static void pow(Conversation c) throws ActionException {
		double y = c.pop().toDouble();
		double x = c.pop().toDouble();
		c.push(new DoubleValue(Math.pow(x, y)));
	}
	
	@Cmd public static void sin(Conversation c) throws ActionException {
		c.push(new DoubleValue(Math.sin(c.pop().toDouble())));
	}
	@Cmd public static void log(Conversation c) throws ActionException {
		c.push(new DoubleValue(Math.log(c.pop().toDouble())));
	}
	@Cmd public static void cos(Conversation c) throws ActionException {
		c.push(new DoubleValue(Math.cos(c.pop().toDouble())));		
	}
	@Cmd public static void tan(Conversation c) throws ActionException {
		c.push(new DoubleValue(Math.tan(c.pop().toDouble())));
	}
}
