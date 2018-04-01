package org.pale.simplechat.commands;

import java.util.List;

import org.pale.simplechat.Category;
import org.pale.simplechat.Conversation;
import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.Cmd;
import org.pale.simplechat.actions.Value;
import org.pale.simplechat.values.CatValue;
import org.pale.simplechat.values.IntValue;
import org.pale.simplechat.values.ListValue;
import org.pale.simplechat.values.StringValue;

public class Categories {
	
	@Cmd public static void iscat(Conversation c) throws ActionException {
		// (string cat -- )
		Category cat = popCat(c);
		String s = c.popString();
		String[] arr = s.split("\\s+");
		c.push(new IntValue(cat.isMatch(arr)?1:0));
	}
	
	// (cat -- string) get name
	@Cmd public static void catname(Conversation c) throws ActionException {
		Category cat = popCat(c);
		c.push(new StringValue(cat.name));
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
		c.push(new CatValue("instcat",listToCat("instcat",lst)));
	}
	
	@Cmd public static void addcat(Conversation c) throws ActionException {
		Category cat = popCat(c);
		addToCat(cat,c.pop());
	}
	
	// (string cat -- subcat) If string is in a category "cat", which of its subcategories is it in?
	// This will return the next level down. Generally done if a pattern finds a category match on "cat"
	@Cmd public static void subcat(Conversation c) throws ActionException {
		Category cat = popCat(c);
		String s = c.popString();
		String[] arr = s.split("\\s+");
		
		Category subcat = cat.getSubcat(arr);
		c.push(new CatValue(subcat.name,subcat));
	}
	
	// (cat -- list of strings) make a list out of all the category's words and lists, and all the categories of which it is made!
	@Cmd public static void cat2list(Conversation c) throws ActionException {
		List<Value> l = popCat(c).catToValueList();
		c.push(new ListValue(l));
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
			c.add(listToCat("listcat",((ListValue)v).list));
		} else if(v instanceof CatValue){
			c.add(((CatValue)v).c);
		} else {
			c.add(v.str());
		}		
	}
	
	private static Category listToCat(String name,List<Value> lst){
		Category c = new Category(name);
		for(Value v : lst){
			addToCat(c,v);
		}
		return c;
	}
	
}
