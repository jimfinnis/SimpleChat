package org.pale.simplechat;

public class Utils {

	public static String[] toLowerCase(String[] s) {
		String[] s2 = new String[s.length];
		for(int i=0;i<s.length;i++) s2[i]=s[i].toLowerCase();
		return s2;
	}

}
