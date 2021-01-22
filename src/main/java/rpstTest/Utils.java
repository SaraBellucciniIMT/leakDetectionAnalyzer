package rpstTest;

import sort.ISort;

public class Utils {

	
	private static int i;
	public static String node = "node";
	public static String pnode = "pnode";
	public static String is_node = "is_node";
	public static String is_pnode = "is_pnode";
	public static String space = " ";
	public static int getId() {
		return i++;
	}
	
	
	
	public static String adjustName(String s) {
		if (s.contains(" "))
			return s.replace(" ", "_");
		return s;
	}
	
	/**
	 * Returns the sort following the struct style namesort = strcut e1|e2|...|en;
	 * @param namesort the name of the sort
	 * @param e the elements in the sort
	 * @return a string following the struct style namesort = strcut e1|e2|...|en;
	 */
	public static String printAsStruct(String namesort, String...e) {
		String string = namesort + " = struct ";
		int i=0;
		for(String s : e) {
			string  +=  s;
			if(i!= e.length-1)
				string += "|";
			i++;
		}
		return string + ";";
		
	}
	
	/**
	 * Returns a string representing the parameters divided by a comma ","
	 * @param data parameters to put as a string
	 * @return a string representing the parameters divided by a comma ","
	 */
	public static String organizeParameterAsString(ISort[] data) {
		String s = "";
		for (int i = 0; i < data.length; i++) {
			s = s + data[i].toString();
			if (i != data.length - 1)
				s = s + ",";
		}
		return s;
	}
	
	public static String organizeParametersAsString(String[] strings) {
		String s = "";
		for (int i = 0; i < strings.length; i++) {
			s = s + strings[i].toString();
			if (i != strings.length - 1)
				s = s + ",";
		}
		return s;
	}
}
