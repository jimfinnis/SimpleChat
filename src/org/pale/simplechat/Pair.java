package org.pale.simplechat;

import org.pale.simplechat.actions.InstructionStream;

/**
 * A pair of pattern and action; each pattern has an action is runs
 * when matched.
 * @author white
 *
 */
public class Pair {
	Pattern pat;
	InstructionStream act;
	public Pair(Pattern p,InstructionStream a){
		pat = p; act = a;
	}
}