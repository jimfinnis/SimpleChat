package org.pale.simplechat;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;

import org.pale.simplechat.patterns.*;

/**
 * A pattern is a template which is matched against user input, which,
 * when matched, assigns some variables and runs an associated Action.
 * @author white
 *
 */
public class Pattern {
	public CharacterIterator iter;
	
	private Node root;

	private String name; // name of pattern, null for anonymous
	
	public String getName(){
		return name==null ? "<ANON>" : name;
	}

	/// get word, assumes current char is alphanumeric, terminated by non-alphanumeric
	public String parseWord(){
		StringBuilder sb = new StringBuilder();
		for(;;){
			char c = iter.current();
			if(Character.isAlphabetic(c) || Character.isDigit(c)){
				sb.append(c);
				iter.next();
			}
			else
				break;
		}
		return sb.toString();	
	}
	
	/// get a label terminated with '='. We are currently on the char before the label, usually '$'
	String parseLabel() throws ParserError{
		StringBuilder sb = new StringBuilder();
		iter.next();
		for(;;){
			char c = iter.current();
			if(c == CharacterIterator.DONE)
				throw new ParserError("pattern label should end with '='");
			iter.next();
			if(c=='=')
				break;
			sb.append(c);
		}
		return sb.toString();	
	}
	

	// parse a white-space separated list of nodes. Parser is on the opening terminator, so we have
	// to skip past it.
	public List<Node> parseNodeList(char terminator) throws ParserError{
		List<Node> nodes = new ArrayList<Node>();
		iter.next(); // skip past opening terminator.
		for(;;){
			skipspaces();
			char c = iter.current();
			if(c==CharacterIterator.DONE)
				throw new ParserError("expected node in pattern or '"+terminator+"', got end of string");
			else if(c==terminator)break;
			else {
				Node n = parseNode();
				nodes.add(n);
			}
		}
		iter.next();
		return nodes;
	}
	
	// make the iterator point to the next non-space
	private void skipspaces(){
		while(Character.isWhitespace(iter.current())){
			iter.next();
		}
	}
	
	/// parse a node. Start parse position is first char in node, end position is just after
	/// last char in node.
	public Node parseNode() throws ParserError{
		char c = iter.current();
		if(c==CharacterIterator.DONE)return null;
		String label;
		if(c=='$'){
			// get a label for the node we are about to parse
			label = parseLabel();
			c = iter.current();
		} else
			label = null;
		Node n;
		
//		System.out.println("GOT at top level: "+c);
		// plain words (or numbers) match themselves
		if(Character.isAlphabetic(c)||Character.isDigit(c)){
			n = new WordNode(this, label);
		} else {
			switch(c){
			case '[':	n = new AnyOfNode(this, label);break;
			case '(':	n = new SequenceNode(this, label);break;
			case '^':	n = new NegateNode(this,label); break;
			case '.':	n = new DotNode(this,label);break;
			case '?':	n = new MaybeNode(this,label);break;
			default:
				System.out.println("CHAR: "+c);
				iter.next();
				n = new DummyNode(this,label,"unknown char "+c);break;
			}
		}
		// we're now on the next char; it may be a post-modifier to wrap the node in a star.
		switch(iter.current()){
		case '+': n = new StarNode(this,n,label,true);break;
		case '*': n = new StarNode(this,n,label,false);break;
		default:break;
		}
		return n;
	}
	
	/// compile the pattern using a recursive descent parser
	public Pattern(String name, String pstring) throws ParserError{
		this.name = name;
		iter = new StringCharacterIterator(pstring);
		iter.first();
		root = parseNode();
		Logger.log("Pattern parsed "+pstring);
	}
	
	
	
	/**
	 * try to match this pattern, returning MatchData, which have invalid set if match failed.
	 * @param s
	 * @return
	 */
	public MatchData match(String s){
		// prepare the data, removing punctuation - we assume this is a single sentence.
		s = s.replaceAll("[^a-zA-Z0-9_]"," ");
		Logger.log("After repl: "+s);
		String[] arr = s.split("\\s+");
		for(String t: arr)Logger.log("Item: **"+t+"**");
		
		MatchData m = new MatchData(arr);
		
		// And here we go. We traverse the nodes, consuming items (or not) in the match data.
		// If a match fails we set the invalid flag, and subsequent nodes should bomb out early.
		
		root.match(m);
		
		// if any tokens remain, that's a fail
		if(!m.allConsumed()){
			m.invalid=true;
			Logger.log("Not all tokens consumed, so invalid");
		}
		return m;
	}
	
	
	
	
}
