/**
 * 
 */
package spec.mcrl2obj;

/**
 * @author sara
 *
 */
public class StructSort extends Sort {

	public static final String empty = "eps";
	
	public StructSort(String name) {
		super(name);
		this.addType(empty);
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public String toString() {
		String s = "sort " + this.getName() + " = struct ";
		int i =0;
		for(String type : this.getTypes()) {
			s = s+ type ;
			if(i != this.getTypes().size()-1)
				s = s + "|";
			i++;
		}
		return s;
	}

}
