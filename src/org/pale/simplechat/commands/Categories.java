package org.pale.simplechat.commands;

import java.util.List;

import org.pale.simplechat.Category;
import org.pale.simplechat.Conversation;
import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.Cmd;
import org.pale.simplechat.actions.InstructionCompiler;
import org.pale.simplechat.actions.Value;
import org.pale.simplechat.values.CatValue;
import org.pale.simplechat.values.IntValue;
import org.pale.simplechat.values.ListValue;

public class Categories {
	
	@Cmd public static void iscat(Conversation c) throws ActionException {
		// (string cat -- )
		Category cat = popCat(c);
		String s = c.popString();
		String[] arr = s.split("\\s+");
		c.push(new IntValue(cat.isMatch(arr)?1:0));
	}

	/**
	 * Makes an category to be stored in an instance or conversation variable.
	 * The stack contains a list, which contains words, space-separated phrase strings, lists (which are turned into
	 * subcategories) and more category values.
	 * @param c
	 * @throws ActionException
	 */
	@Cmd public static void cat(Conversation c) throws ActionException {
		List<Value> lst = c.popList();
		c.push(new CatValue("instcat",listToCat(lst)));
	}
	
	@Cmd public static void addcat(Conversation c) throws ActionException {
		Category cat = popCat(c);
		addToCat(cat,c.pop());
	}

	
	
	private static Category popCat(Conversation c) throws ActionException {
		Value v = c.pop();
		if(!(v instanceof CatValue))
			throw new ActionException("expected a category");
		Category cat = ((CatValue)v).c;
		return cat;
	}
	

	private static void addToCat(Category c,Value v){
		if(v instanceof ListValue){
			c.add(listToCat(((ListValue)v).list));
		} else if(v instanceof CatValue){
			c.add(((CatValue)v).c);
		} else {
			c.add(v.str());
		}		
	}
	
	private static Category listToCat(List<Value> lst){
		Category c = new Category();
		for(Value v : lst){
			addToCat(c,v);
		}
		return c;
	}
}
