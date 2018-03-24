package org.pale.simplechat;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pale.simplechat.patterns.MatchData;

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
 * 
 * Category leaves can also be arrays of words, which have to be matched in the order
 * they are given, so  "foo","bar" will match "foo bar" and "foo. Bar". 
 * @author white
 *
 */
public class Category {
	String name;
	private Set<String> words = new HashSet<String>(); // leaf nodes of this category
	private Set<Category> cats = new HashSet<Category>(); // branches
	private List<String[]> lists = new ArrayList<String[]>(); // more leaf nodes for multiple word leafs, sorted in descending length
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
	
	public Category(String name){
		this.name = name;
	}


	public void add(String phrase){
		String[] s = Utils.toLowerCase(phrase.split("[_\\s]+"));
		if(s.length > 1)
			lists.add(s);
		else
			words.add(s[0]);
	}

	public void add(Category subcat){
		if(subcat.containsRecurse(this))
			System.out.println("Category recursion detected, not adding "+subcat.name+" to "+name);
		else
			cats.add(subcat);
	}

	// apply various modifiers to a category
	private void parseModifier(StreamTokenizer tok) throws IOException, ParserError{
		for(;;){
			switch(tok.nextToken()){
			case '+':
				// add a suffix to every word (not lists or subcats) in the cat, and put that in as duplicates.
				// This means [say talk]/+ing is [say talk saying talking].
				if(tok.nextToken()!=StreamTokenizer.TT_WORD)
					throw new ParserError("Expected suffix after '+' in category modifier");
				List<String> toAdd = new ArrayList<String>();
				for(String w: words)
					toAdd.add(w+tok.sval);
				for(String w: toAdd)
					add(w);
				break;
			default:
				tok.pushBack();return;
			}
		}
	}



	// This compiles ~name=[...], where [...] consists of words or subcategories.
	// Subcategories are either ~name=[...] again, or just ~name in which case the category
	// is already defined.
	// Names will be put into the given namespace.
	// Parsing starts *after* the tilde.

	static Category parseCat(Bot b,Tokenizer tok) throws ParserError, IOException{

		tok.setForCats();
		
		if(tok.nextToken()!=StreamTokenizer.TT_WORD)
			throw new ParserError("expected name in category");
		String name = tok.sval;
		if(tok.nextToken()!='='){
			tok.pushBack();
			// return pre-existing cat
			Category c = b.getCategory(name,false);
			if(c==null)
				throw new ParserError("cannot find category ~"+name);
			return c;
		} else {
			// we're defining a new one!
			if(tok.nextToken()!='[')
				throw new ParserError("expected [ in category definition");
			Category c = b.getCategory(name,false);
			outerloop:
				for(;;){
					int t = tok.nextToken();
					switch(t){
					case StreamTokenizer.TT_WORD:
					case '\'':case '\"':
						c.add(tok.sval);
						break;	
					case '~':
						c.add(parseCat(b,tok));
						break;
					case ']':break outerloop;
					default: throw new ParserError("error in category");
					}
				}
			// check for post modifiers
			if(tok.nextToken()=='/')
				c.parseModifier(tok);
			else
				tok.pushBack();


			// sort the lists by length so longer lists are tried first
			Collections.sort(c.lists,new Comparator<String[]>(){
				@Override
				public int compare(String[] arg0, String[] arg1) {
					return arg1.length-arg0.length;
				}
			});
			tok.setDefault();
			
			return c;
		}
	}


	public void dump(){
		_dump(0);
	}
	private void _dump(int level){
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<=level;i++)sb.append("-");
		String pf = sb.toString();
		for(String w: words)
			Logger.log(Logger.ALWAYS,pf+w);
		for(String[] a: lists){
			sb = new StringBuilder();
			for(String w: a)
				sb.append(w+",");
			Logger.log(Logger.ALWAYS,pf+sb.toString());
		}
		for(Category c:cats){
			c._dump(level+1);
		}
	}
	
	
	private boolean containsRecurse(Category c,int depth){
		if(depth>10)
			return true;
		if(cats.contains(c))
			return true;
		for(Category cc: cats){
			if(cc.containsRecurse(c,depth+1))
				return true;
		}
		return false;
	}
	
	public boolean containsRecurse(Category c) {
		return containsRecurse(c,0);
	}

	public boolean match(MatchData m) {
		// first, try the words.
		if(words.contains(m.cur())){
			m.consumed = m.consume();
			return true;			
		}
		// then try the lists. This returns 0 or the number of words matches.
		int n = matchesList(m.words,m.pos);
		if(n>0){
			// if we got one, consume those words and concatenate into a string for
			// the new consumed data from this node.
			StringBuilder sb = new StringBuilder();;
			for(int i=0;i<n;i++){
				sb.append(m.consume());
				if(i!=n-1){
					sb.append(" ");
				}
			}
			m.consumed = sb.toString();
			return true;
		}
		// otherwise try the subcategories.
		for(Category sc: cats){
			if(sc.match(m))
				return true;
		}
		// no match.
		return false;
	}

	// return true if the string is a match for the category. This is used for action language
	// match testing.

	public boolean isMatch(String[] s){
		s = Utils.toLowerCase(s);
		// first, try the words. This is only used when length is 1.
		if(s.length==1 && words.contains(s[0])){
			return true;			
		}
		// then try the lists. This returns 0 or the number of words matches.
		// In this function we only have a positive match if the number of words
		// matched is the same as the number passed in (i.e. "red dog with nose" will not match the list "red dog").
		int n = matchesList(s,0);
		if(n == s.length){
			return true;
		}
		// otherwise try the subcategories.
		for(Category sc: cats){
			if(sc.isMatch(s))
				return true;
		}
		// no match.
		return false;

	}

	private int matchesList(String[] a, int pos) {
		// Do the words in the array a starting at pos match any of our lists?
		// If so, return the number of items in the matching list. Match only complete lists, obviously.
		// There may well be a quicker way to do this.

		int maxwords = a.length-pos;
		outerloop:
			for(String[] lst: lists){
				if(lst.length > maxwords)continue; // too many words to match
				for(int i=0;i<lst.length;i++){
					if(!lst[i].equals(a[i+pos]))continue outerloop; // match failed, abandon
				}
				// all matched OK
				return lst.length;
			}
		return 0; // nope
	}		
}
