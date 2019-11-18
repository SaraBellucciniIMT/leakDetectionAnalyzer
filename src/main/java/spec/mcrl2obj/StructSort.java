/**
 * 
 */
package spec.mcrl2obj;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Triple;

import io.pet.PET;

/**
 * @author sara
 *
 */
public class StructSort extends Sort {

	private Set<Triple<String,PET,Integer>> triple = new HashSet<Triple<String,PET,Integer>>();
	
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
	
	public void addTriple(Triple<String, PET,Integer> t) {
		this.triple.add(t);
	}
	
	public Set<Triple<String,PET,Integer>> getPrivateTriple(){
		Set<Triple<String,PET,Integer>> privatetriple = new HashSet<Triple<String,PET,Integer>>();
		for(Triple<String,PET,Integer> t : triple) {
			if(t.getMiddle() != null)
				privatetriple.add(t);
		}
		return privatetriple;
	}
	
	public Triple<String,PET,Integer> getTripleByName(String name) {
		for(Triple<String, PET, Integer> t : triple) {
			if(t.getLeft().equals(name))
				return t;
		}
		return null;
	}

}
