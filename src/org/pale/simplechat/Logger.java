package org.pale.simplechat;

public class Logger {
	public interface Listener {
		void log(String s);
	}
	
	static class DefaultListener implements Listener {
		@Override
		public void log(String s) {
			System.out.println("LOG: "+s);
		}
	}
	
	public static void log(String s){
		listener.log(s);
	}
	
	static void setListener(Listener l){
		listener = l;
	}

	private static Listener listener = new DefaultListener();

}
