package org.pale.simplechat;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Categories are like concepts in ChatScript, and have a similar syntax.
 * A word can be a member of several categories, and categories can be also
 * be members of several categories.
 *
 * The essential function is "is word A in category B". Given that categories form
 * a hierarchy, we could find a word's leaf category, and then go up the hierarchy to
 * see if we find the category we're looking for. Unfortunately words can be in
 * more than one category. So we probably need to search down - go through
 * the child categories until we find the word.
 * @author white
 *
 */
public class Category {
	private Set<String> words = new HashSet<String>(); // leaf nodes of this category
	private Set<Category> cats = new HashSet<Category>(); // branches
	
	/**
	 * depth first search of this category
	 * @param w
	 * @return
	 */
	public boolean contains(String w){
		if(words.contains(w))return true;
		for(Category c: cats){
			if(c.contains(w))return true;
		}
		return false;
	}
	
	public static Map<String,Category> globalCats = new HashMap<String,Category>();
	
	public static Category get(Map<String,Category> ns,String name){
		if(ns.containsKey(name))return ns.get(name);
		if(globalCats.containsKey(name))return globalCats.get(name);
		else return null;	
	}
	
	
	// This compiles ~name=[...], where [...] consists of words or subcategories.
	// Subcategories are either ~name=[...] again, or just ~name in which case the category
	// is already defined.
	// Names will be put into the given namespace.
	// Parsing starts *after* the tilde.

	static Category parseCat(Map<String,Category> ns,StreamTokenizer tok) throws ParserError, IOException{
		if(tok.nextToken()!=StreamTokenizer.TT_WORD)
			throw new ParserError("expected name in category");
		String name = tok.sval;
		if(tok.nextToken()!='='){
			tok.pushBack();
			// return pre-existing cat
			Category c = get(ns,name);
			if(c==null)
				throw new ParserError("cannot find category ~"+name);
			return c;
		} else {
			// we're defining a new one!
			if(tok.nextToken()!='[')
				throw new ParserError("expected [ in category definition");
			Category c = new Category();
			ns.put(name,c);
			outerloop:
			for(;;){
				int t = tok.nextToken();
				switch(t){
				case StreamTokenizer.TT_WORD:
					c.words.add(tok.sval.toLowerCase());
					break;
				case '~':
					c.cats.add(parseCat(ns,tok));
					break;
				case ']':break outerloop;
				}
			}
			ns.put(name,c);
			return c;
		}
	}
}
