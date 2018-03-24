package org.pale.simplechat;


public class Utils {

	public static String[] toLowerCase(String[] s) {
		String[] s2 = new String[s.length];
		for(int i=0;i<s.length;i++) s2[i]=s[i].toLowerCase();
		return s2;
	}
	
	public static String removeSuffix(String s,String suffix){
		int pos = s.lastIndexOf(suffix);
		if (pos == s.length()-suffix.length()) {
			return s.substring(0, pos);
		} else {
			return s;
		}
	}
	
}
