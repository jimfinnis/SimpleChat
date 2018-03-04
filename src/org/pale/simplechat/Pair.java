package org.pale.simplechat;

import org.pale.simplechat.actions.Action;

/**
 * A pair of pattern and action; each pattern has an action is runs
 * when matched.
 * @author white
 *
 */
public class Pair {
	Pattern pat;
	Action act;
	public Pair(Pattern p,Action a){
		pat = p; act = a;
	}
}