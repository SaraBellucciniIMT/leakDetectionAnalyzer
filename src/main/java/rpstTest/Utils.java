package rpstTest;

import spec.mcrl2obj.DataParameter;

public class Utils {

	public Utils() {
		// TODO Auto-generated constructor stub
	}
	private static int i;
	
	public static int getId() {
		return i++;
	}
	
	public static String adjustName(String s) {
		if (s.contains(" "))
			return s.replace(" ", "_");
		return s;
	}
	public static String organizeParameterAsString(DataParameter[] d) {
		String s = "";
		for (int i = 0; i < d.length; i++) {
			s = s + d[i];
			if (i != d.length - 1)
				s = s + ",";
		}
		return s;
	}
}
