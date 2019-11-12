/**
 * 
 */
package spec.mcrl2obj;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Triple;

/**
 * @author sara
 *
 */
public class StructSort extends Sort {

	//public static final String empty = "eps";
	
	private Set<Triple<String,Boolean,Integer>> triple = new HashSet<Triple<String,Boolean,Integer>>();
	
	public StructSort(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public String toString() {
		String s = "sort " + this.getName() + " = struct ";
		int i =0;
		for(String type : this.getTypes()) {
			if(type.contains(" "))
				type = type.replace(" ", "_");
			s = s+ type ;
			if(i != this.getTypes().size()-1)
				s = s + "|";
			i++;
		}
		return s;
	}
	
	public void addTriple(Triple<String, Boolean,Integer> t) {
		this.triple.add(t);
	}
	
	public Set<Triple<String,Boolean,Integer>> getTripleByName(String name) {
		Set<Triple<String,Boolean,Integer>> triplename = new HashSet<Triple<String,Boolean,Integer>>();
		for(Triple<String,Boolean,Integer> t : triple) {
			if(t.getLeft().equals(name))
				triplename.add(t);
		}
		return triplename;
	}

}
