package org.pale.simplechat.actions;

import java.util.ArrayList;
import java.util.List;

import org.pale.simplechat.Pair;

/**
 * Ugly, because the stack can contain anything and we can't have unions in Java.
 * @author white
 *
 */

public abstract class Value {
	public String str() {
		return "??";
	}
	
	public int toInt() throws ActionException{
		throw new ActionException("cannot convert value to int");
	}
	
	public double toDouble() throws ActionException{
		throw new ActionException("cannot convert value to double");
	}	
}
