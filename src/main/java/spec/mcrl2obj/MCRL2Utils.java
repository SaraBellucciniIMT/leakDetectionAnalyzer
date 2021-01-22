package spec.mcrl2obj;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;


public class MCRL2Utils {

	public static final String node = "node";
	public static final String is_n = "is_node";
	public static final String pnode = "pnode";
	public static final String is_pn = "is_pnode";
	public static final String v = "value";
	public static final String pv = "pvalue";
	public static final String unionf = "union";
	public static final String emptyf = "empty";
	public static final String head = "head";
	public static final String tail = "tail";
	public static final String frt = "frt";
	public static final String snd = "snd";
	public static final String pair = "pair";
	public static final String sort = "sort";
	public static final String map = "map";
	public static final String var = "var";
	public static final String eqn = "eqn";
	public static final String act = "act";
	public static final String proc = "proc";
	public static final String exist = "exists";

	/**
	 * Prints f(arg1,arg2,...,argn)
	 * 
	 * @param f   function name
	 * @param arg arguments of the faciton
	 * @return
	 */
	public static String printf(String f, String... arg) {
		String s = f + "(";
		for (int i = 0; i < arg.length; i++) {
			s = s + arg[i];
			if (i != arg.length - 1)
				s = s + ",";
		}
		s = s + ")";
		return s;
	}

	/**
	 * Returns namef : domain1 # domain2 # ... # domainn -> codomain
	 * 
	 * @param namef    name function
	 * @param codomain unique type of function's result
	 * @param domain   type of the inputs divided by #
	 * @return namef = domain1 # domain2 # ... # domainn -> codomain
	 */

	public static String printMap(String namef, String codomain, String... domain) {
		String s = namef + ":";
		for (int i = 0; i < domain.length; i++) {
			s = s + domain[i];
			if (i != domain.length - 1)
				s = s + "#";
		}
		s = s + "->" + codomain + "; \n";
		return s;
	}

	/**
	 * Prints f = true
	 * 
	 * @param f function
	 * @return
	 */
	public static String printtruef(String f) {
		return f + " = " + true + ";\n";
	}

	/**
	 * Prints f = false;
	 * 
	 * @param f funciton
	 * @return
	 */
	public static String printfalsef(String f) {
		return f + "=" + false + ";\n";
	}

	/**
	 * Returns (ifs) -> namef = resulf 
	 * @param ifs condition to be checked
	 * @param namef name of the function
	 * @param resultf result of the function  if exists
	 * @return (ifs) -> namef = resulf 
	 */
	public static String printifeqn(String ifs, String namef, String resultf) {
		String s = "";
		if (!resultf.isEmpty())
			s = "(" + ifs + ") -> " + namef + " = " + resultf + ";\n";
		return s;
	}
	
	public static String printAnd(String...strings) {
		String s ="";
		if(strings.length != 0) {
			s = strings[0];
			int i=1;
			while(i<strings.length) {
				s += "&&" +strings[i]; 
				i++;
			}
		}
		return s;		
	}
	
	public static String printOr(String...strings) {
		String s ="";
		if(strings.length != 0) {
			s = strings[0];
			int i=1;
			while(i<strings.length) {
				s += "||" +strings[i]; 
				i++;
			}
		}
		return s;		
	}
	
	/**
	 * Returns the string f(frt(pvalue(head(var))))
	 * @param f the name of the function
	 * @param var the name of the variable
	 * @return the string f(frt(pvalue(head(var))))
	 */
	public static String printFFrtPvH(String f,String var) {
		return printf(f, printFrtPvH(var));
	}
	
	/**
	 * Returns the string frt(pvalue(head(var)))
	 * @param var the name of the variable
	 * @return the string frt(pvalue(head(var)))
	 */
	public static String printFrtPvH(String var) {
		return printf(frt, printPvH(var));
	}
	
	/**
	 * Returns the string snd(pvalue(head(var)))
	 * @param var the name of the variable
	 * @return the string snd(pvalue(head(var)))
	 */
	public static String printSndPvH(String var) {
		return printf(snd, printPvH(var));
	}
	
	/**
	 * Returns the string head(var)
	 * @param var the name of the variable
	 * @return the string head(var)
	 */
	public static String printH(String var) {
		return printf(head, var);
	}

	/**
	 * Returns the string tail(var)
	 * @param var the name of the variable
	 * @return the string tail(var)
	 */
	public static String printT(String var) {
		return printf(tail,var);
	}
	
	/**
	 * Returns the string pvalue(head(var))
	 * @param var the name of the variable
	 * @return the string pvalue(head(var))
	 */
	public static String printPvH(String var) {
		return printf(pv, printH(var));
	}
	
	/**
	 * Returns the string is_pnode(head(var))
	 * @param var the name of the variable
	 * @return the string is_pnode(head(var))
	 */
	public static String printIsPnH(String var) {
		return printf(is_pn, printH(var));
	}
	
	public static Set<String> parseFSMTaskLine(String line) {
		String inCurlyBrackets = line.substring(line.indexOf("{") + 1, line.lastIndexOf("}"));
		String[] reading = new String[0];
		String st="";
		int n_bracket =0;
		for(int i=0; i< inCurlyBrackets.length() ; i++) {
			String c = String.valueOf(inCurlyBrackets.charAt(i));
			if ((c.equals(",") && n_bracket ==0)) {
				reading = ArrayUtils.add(reading, st);
				st = "";
				continue;
			}
			if(c.equals("("))
				n_bracket++;
			if(c.equals(")"))
				n_bracket--;
			st +=c;
		}
		reading = ArrayUtils.add(reading, st);
		return parseNodePNode(reading);
	}
	
	private static Set<String> parseNodePNode(String[] strings) {
		Set<String> setdata = new HashSet<String>();
		for(String s : strings) {
			String d = parsePNode(s);
			if(d == null) {
				d = parseNode(s);
			}
			setdata.add(d);
		}
		return setdata;
	}
	
	private static String parseNode(String s) {
		s = s.strip();
		for(int i=0; i<node.length(); i++) {
			String c_s = String.valueOf(s.charAt(i));
			String c_node = String.valueOf(node.charAt(i));
			if(!c_s.equals(c_node)) {
				return null;
			}
		}
		s = s.substring(node.length()+1, s.length()-1);
		return s;
	}
	
	private static String parsePNode(String s) {
		s = s.strip();
		for(int i=0; i<pnode.length(); i++) {
			String c_s = String.valueOf(s.charAt(i));
			String c_node = String.valueOf(pnode.charAt(i));
			if(!c_s.equals(c_node)) {
				return null;
			}
		}
		s = s.substring(node.length()+pair.length()+3 , s.length()-2);
		String[] split = s.split(",");
		String petname = split[0].substring(0, split[0].indexOf("("));
		String name = split[0].replace(petname+"(", "").replace(")", "");
		return name;
	}
}
