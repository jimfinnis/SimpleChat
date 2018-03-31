package org.pale.simplechat;

import org.pale.simplechat.actions.Value;


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
	
	// java 8 has this but bukkit advised java 7.
	public static String join(String[] lst, String js){
		StringBuilder sb = new StringBuilder();
		boolean first=true;
		for(String v:lst)
		{
			if(first)first=false;
			else sb.append(js);
			sb.append(v);
		}
		return sb.toString();
	}
	
}
