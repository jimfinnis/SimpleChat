package org.pale.simplechat.actions;

import java.util.Map;

import org.pale.simplechat.Conversation;
import org.pale.simplechat.values.ListValue;
import org.pale.simplechat.values.MapValue;
import org.pale.simplechat.values.NoneValue;

public class Collections  {

	public static class NewHashInstruction extends Instruction {
		@Override
		int execute(Conversation c) throws ActionException {
			c.push(new MapValue());
			return 1;
		}
	}

	public static class AppendInstruction extends Instruction {
		@Override
		int execute(Conversation c) throws ActionException {
			Value append = c.pop();
			Value lst = c.peek();
			if(!(lst instanceof ListValue)){
				// must be a appending to a hash, in which case this is the key
				Value key = c.pop();
				Value map = c.peek();
				if(!(map instanceof MapValue)){
					throw new ActionException("must append to map or list");
				}
				((MapValue)map).map.put(key.str(), append);
			} else
				((ListValue)lst).list.add(append); // should this be a clone??
			return 1;
		}
	}

	public static class NewListInstruction extends Instruction {
		@Override
		int execute(Conversation c) throws ActionException {
			c.push(new ListValue());
			return 1;
		}
	}

	public static class SymbolGetInstruction extends Instruction {
		String sym;
		public SymbolGetInstruction(String sym) {
			this.sym=sym;
		}
		@Override
		int execute(Conversation c) throws ActionException {
			Map<String,Value> m = c.popMap();
			Value v = m.get(sym);
			if(v==null)v=NoneValue.instance;
			c.push(v);
			return 1;
		}
	}

	public static class SymbolSetInstruction extends Instruction {
		String sym;
		public SymbolSetInstruction(String sym) {
			this.sym=sym;
		}
		@Override
		int execute(Conversation c) throws ActionException {
			Map<String,Value> m = c.popMap();
			Value v = c.pop();
			m.put(sym,v);
			return 1;
		}
	}

}
