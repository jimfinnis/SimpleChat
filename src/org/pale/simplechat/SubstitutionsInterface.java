package org.pale.simplechat;

/**
 * Used to process strings - 
 * handles both substitution list and a special item for
 * processing the substitutions inherited from the parent.
 * @author white
 *
 */
public interface SubstitutionsInterface {
	String process(String s);

}
