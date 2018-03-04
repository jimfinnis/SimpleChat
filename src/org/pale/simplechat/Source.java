package org.pale.simplechat;

/**
 * A speaker, with whom a BotInstance is speaking.
 * @author white
 *
 */
public class Source {
	/**
	 * You can override this to receive responses from the bot.
	 * @param s
	 */
	public void receive(String s){}
}
