package org.pale.simplechat.commands;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.pale.simplechat.Conversation;
import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.Cmd;
import org.pale.simplechat.actions.Value;
import org.pale.simplechat.values.IntValue;
import org.pale.simplechat.values.NoneValue;
import org.pale.simplechat.values.StringValue;

public class Lists {
	@Cmd public static void get(Conversation c) throws ActionException {
		// (idx list -- val)
		List<Value> lst = c.popList();
		int key = c.pop().toInt();
		if(key>=0 && key<lst.size())
			c.push(lst.get(key));
		else
			c.push(NoneValue.instance);
	}
	
	@Cmd public static void set(Conversation c) throws ActionException {
		// (val idx list -- )
		List<Value> lst = c.popList();
		int key = c.pop().toInt();
		Value v = c.pop();
		if(key>=0 && key<lst.size())
			lst.set(key, v);
	}
	
	@Cmd public static void pop(Conversation c) throws ActionException {
		// (list -- val)
		List<Value> lst = c.popList();
		if(lst.size()>0)
			c.push(lst.remove(lst.size()-1));
		else
			c.push(NoneValue.instance);
	}
	
	@Cmd public static void shift(Conversation c) throws ActionException {
		// (list -- val)
		List<Value> lst = c.popList();
		if(lst.size()>0)
			c.push(lst.remove(0));
		else
			c.push(NoneValue.instance);
	}

	@Cmd public static void push(Conversation c) throws ActionException {
		// (val list --)
		List<Value> lst = c.popList();
		Value v = c.pop();
		lst.add(v);
	}

	@Cmd public static void unshift(Conversation c) throws ActionException {
		// (list -- val)
		List<Value> lst = c.popList();
		Value v = c.pop();
		lst.add(0, v);
	}

	@Cmd public static void len(Conversation c) throws ActionException {
		c.push(new IntValue(c.popList().size()));
	}
	
	@Cmd public static void choose(Conversation c) throws ActionException {
		List<Value> lst = c.popList();
		int n = ThreadLocalRandom.current().nextInt(lst.size());
		c.push(lst.get(n));
	}
	
	@Cmd public static void join(Conversation c) throws ActionException {
		// (list string -- string)
		String js = c.popString();
		List<Value> lst = c.popList();
		StringBuilder sb = new StringBuilder();
		boolean first=true;
		for(Value v:lst)
		{
			if(first)first=false;
			else sb.append(js);
			sb.append(v.str());
		}
		c.push(new StringValue(sb.toString()));
	}

}