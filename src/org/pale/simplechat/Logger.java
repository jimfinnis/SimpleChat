package org.pale.simplechat;

public class Logger {
	public static final int PATTERN=1;
	public static final int ACTION=2;
	public static final int CONFIG=4;
	public static final int LOAD=8;
	public static final int FATAL=16;
	public static final int ALWAYS=32;
	
	public interface Listener {
		void log(String s);
	}
	
	static class DefaultListener implements Listener {
		@Override
		public void log(String s) {
			System.out.println("LOG: "+s);
		}
	}
	
	public static void log(int type,String s){
		listener.log(s);
	}
	
	public static void setListener(Listener l){
		listener = l;
	}

	private static Listener listener = new DefaultListener();

}
