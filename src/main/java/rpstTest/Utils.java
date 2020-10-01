package rpstTest;

import spec.mcrl2obj.DataParameter;

public class Utils {

	
	private static int i;
	private static String dataID = "dataID";
	public static String node = "node";
	public static String pnode = "pnode";
	public static String is_node = "is_node";
	public static String is_pnode = "is_pnode";
	public static int getId() {
		return i++;
	}
	
	public static String getDataID() {
		return dataID + i++;
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
