package org.pale.simplechat;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;

import org.pale.simplechat.patterns.AnyOfNode;
import org.pale.simplechat.patterns.CategoryNode;
import org.pale.simplechat.patterns.DotNode;
import org.pale.simplechat.patterns.MatchData;
import org.pale.simplechat.patterns.MaybeNode;
import org.pale.simplechat.patterns.NegateNode;
import org.pale.simplechat.patterns.Node;
import org.pale.simplechat.patterns.SequenceNode;
import org.pale.simplechat.patterns.StarNode;
import org.pale.simplechat.patterns.StoredPatternNode;
import org.pale.simplechat.patterns.WordNode;

/**
 * A pattern is a template which is matched against user input, which,
 * when matched, assigns some variables and runs an associated Action.
 * @author white
 *
 */
public class Pattern {
	private Node root;

	private String name; // name of pattern or string if anonymous, used in debugging output

	public String getName(){
		return name;
	}

	// parse a white-space separated list of nodes. Parser is after the opening 'terminator'.
	public List<Node> parseNodeList(Bot b,char terminator,Node parent, StreamTokenizer tok) throws ParserError, IOException{
		List<Node> nodes = new ArrayList<Node>();
		for(;;){
			if(tok.nextToken() == terminator)
				break;
			tok.pushBack();
			Node n = parseNode(b,parent,tok);
			nodes.add(n);
		}
		return nodes;
	}
	
	/// parse a node. 
	public Node parseNode(Bot b,Node parent,StreamTokenizer tok) throws ParserError, IOException{
		String label;
		if(tok.nextToken() == '$'){
			// get a label for the node we are about to parse
			if(tok.nextToken()!=StreamTokenizer.TT_WORD)
				throw new ParserError("expected pattern node label after $");
			label = tok.sval; 
			if(tok.nextToken()!='=')
				throw new ParserError("expected = after pattern node label");
		} else {
			label = null;
			tok.pushBack();
		}
		Node n;

		switch(tok.nextToken()){
		case StreamTokenizer.TT_WORD:
			// plain words (or numbers) match themselves
			n = new WordNode(this, label, parent,tok.sval);
			break;
		case '[':	n = new AnyOfNode(b,this, label,parent,tok);break;
		case '(':	n = new SequenceNode(b,this, label,parent,tok);break;
		case '^':	n = new NegateNode(b,this,label,parent,tok); break;
		case '.':	n = new DotNode(this,label,parent,tok);break;
		case '?':	n = new MaybeNode(b,this,label,parent,tok);break;
		case '~':	n = new CategoryNode(b,this,label,parent,tok);break;
		case '&':	n = new StoredPatternNode(b,this,label,parent,tok);break;
		default:
			System.out.println("Wut");
			throw new ParserError("unknown char in pattern: "+tok.ttype);
		}

		// we're now on the next char; it may be a post-modifier to wrap the node in a star.
		switch(tok.nextToken()){
			case '+': n = new StarNode(this,n,label,true,parent,tok);break;
			case '*': n = new StarNode(this,n,label,false,parent,tok);break;
			default:
				tok.pushBack();
				break;
		}
		return n;
	}

	static private int anonCt=0;
	/**
	 * parse a pattern, tokeniser is just before first token in pattern proper.
	 * @param b
	 * @param name
	 * @param pstring
	 * @throws ParserError
	 * @throws IOException 
	 */
	public Pattern(Bot b, String name, StreamTokenizer tok) throws ParserError, IOException{
		if(name==null)
			this.name = "anonymous"+anonCt++;
		else
			this.name = name;
		Logger.log(Logger.PATTERN, "Parsing pattern "+this.name);
		root = parseNode(b,null,tok);
		Logger.log(Logger.PATTERN,"Pattern parsed "+this.name);
	}
	
	
	
	/**
	 * try to match this pattern, returning MatchData, which have invalid set if match failed.
	 * @param s
	 * @return
	 */
	public MatchData match(String s){
		// prepare the data, removing punctuation - we assume this is a single sentence.
		s = s.replaceAll("[^a-zA-Z0-9_]"," ");
		Logger.log(Logger.PATTERN,"After repl: "+s);
		String[] arr = s.split("\\s+");
		for(String t: arr)Logger.log(Logger.PATTERN,"Item: **"+t+"**");
		
		MatchData m = new MatchData(arr);
		
		// attempt to match
		match(m);
		
		// if any tokens remain, that's a fail
		if(!m.allConsumed()){
			m.invalid=true;
			Logger.log(Logger.PATTERN,"Not all tokens consumed, so invalid");
		}
		return m;
	}
	
	public void match(MatchData m){
		// And here we go. We traverse the nodes, consuming items (or not) in the match data.
		// If a match fails we set the invalid flag, and subsequent nodes should bomb out early.
		root.match(m);
	}
}
