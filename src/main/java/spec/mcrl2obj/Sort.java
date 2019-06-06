package spec.mcrl2obj;

import java.util.HashSet;
import java.util.Set;

public class Sort {

	private String name;
	private static final String struct = "struct";
	protected static final String empty = "eps";
	private Set<String> types;
	
	public Sort(String name) {
		this.name = name;
		types = new HashSet<String>();
		types.add(empty);
	}
	
	public void addType(String type) {
		this.types.add(type);
	}
	
	public Set<String> getTypes(){
		return this.types;
	}
	

	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		String s = "sort " + name + " = "+ struct +" ";
		int i =0;
		for(String type : types) {
			s = s+ type ;
			if(i != types.size()-1)
				s = s + "|";
			i++;
		}
		
		return s;
	}
	
	
}
