package org.pale.simplechat.patterns;

import org.pale.simplechat.Logger;
import org.pale.simplechat.Pattern;

abstract public class Node {
	Node(Pattern p,String lab,Node parent){
		pattern = p;
		label = lab;
		next = null;
		this.parent = parent;
	}
	String label; // if non-null, a value is stored during parsing into a hash
	final Pattern pattern; // containing pattern
	Node parent; // parent node or null if this is the root
	Node next; // in sequences, references the next node; otherwise null

	// parses the string with the node, advancing the position in MatchData to consume
	// tokens until we either succeed or fail (in which case invalid is set in the data).
	public abstract void match(MatchData m);

	public void log(String s){
		String pn;
		if(parent == null)pn="(none)";
		else pn = parent.getClass().getSimpleName();
		Logger.log(Logger.PATTERN,"NODE "+this.getClass().getSimpleName()+"/"+pn+": "+s);
	}

	// find the next node in the entire structure, even if it means going up a level or two.
	// In other words, if we are at the end of a sequence, we could still be inside another
	// sequence at the level above. And if we're at the end of that, we could still be inside
	// yet another and so on.
	
	Node getNextNode() {
		Node n = this;
		while(n!=null){
			// we have a next node, so return it.
			if(n.next!=null)return n.next;
			// no next node here .. go up a level
			n = n.parent;
		}
		return null; // got to the top of the tree without a next node.
	}

	/// used to link child nodes into a parent's SequenceNode linked list, such as with NegateNode
	public void nextLinkChild(Node n) {}
}