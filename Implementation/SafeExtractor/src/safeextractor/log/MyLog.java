package safeextractor.log;

import java.util.ArrayList;
import java.util.List;

public class MyLog {
	static List<String> message=new ArrayList<String>();
	public static void show() {
		System.out.println("\n------------------------start\tto\tshow\tlog------------------------");
		message.forEach(v->System.out.println(v));
	}
	public static void add(Object s) {
		if(s!=null) {
		   message.add(s.toString());
		}else {
			 message.add("");
		}
	}
	public static void clear() {
		message.clear();
	}
}
