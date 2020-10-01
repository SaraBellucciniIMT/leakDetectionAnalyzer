/**
 * 
 */
package sort;

import java.util.Set;

import sort.Sort;

/**
 * @author sara
 *
 */
public abstract class AbstractStructSort implements Sort {

	protected Set<String> type;
	
	
	protected String printStruct() {
		String s = " = struct ";
		int i = 0;
		for (String t : type) {
			s = s + t;
			if (i != type.size() - 1)
				s = s + "|";
			i++;
		}
		return s;
	}
	

}
