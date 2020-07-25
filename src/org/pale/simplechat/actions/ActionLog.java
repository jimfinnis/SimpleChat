package org.pale.simplechat.actions;

import java.util.Deque;
import java.util.LinkedList;

import org.pale.simplechat.Logger;

/**
 * This static class manages a limited buffer of log entries which
 * are printed (well, logged) when an exception happens in an action
 * @author white
 *
 */
public class ActionLog {
	private static Deque<String> data = new LinkedList<String>();
	private static final int LIMIT = 32;

	static void write(String s){
		data.addLast(s);
		if(data.size()>LIMIT)
			data.removeFirst();
	}
	
	public static void show(){
		for(String s: data){
			Logger.log(Logger.ACTION,"ACTION LOG: "+s);
		}
	}
}
