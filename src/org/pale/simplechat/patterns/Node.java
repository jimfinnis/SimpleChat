package org.pale.simplechat.patterns;

import org.pale.simplechat.Logger;
import org.pale.simplechat.Pattern;

abstract public class Node {
	Node(Pattern p,String lab){
		pattern = p;
		label = lab;
		next = null;
	}
	String label; // if non-null, a value is stored during parsing into a hash
	final Pattern pattern; // containing pattern
	Node next; // in sequences, references the next node; otherwise null
	// parses the string with the node, advancing the position in MatchData to consume
	// tokens until we either succeed or fail (in which case invalid is set in the data).
	public abstract void parse(MatchData m);
	
	public void log(String s){
		Logger.log("NODE "+this.getClass().getSimpleName()+": "+s);
	}

	/// used to link child nodes into a parent's SequenceNode linked list, such as with NegateNode
	public void nextLinkChild(Node n) {}
}