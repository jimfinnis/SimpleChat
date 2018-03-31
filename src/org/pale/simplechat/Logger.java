package org.pale.simplechat;

public class Logger {
	public static final int ALWAYS=0;
	public static final int PATTERN=1;
	public static final int ACTION=2;
	public static final int CONFIG=4;
	public static final int LOAD=8;
	public static final int FATAL=ALWAYS;
	public static final int CATMATCH = 16;
	public static final int ALL = 65535;
	
	private static int log = 0;
	
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
		if(type == ALWAYS || (type & log)!=0)
			listener.log(s);
	}
	
	public static void setLog(int l){
		log = l;
	}
	
	public static void setListener(Listener l){
		listener = l;
	}

	private static Listener listener = new DefaultListener();

}
