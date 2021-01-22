package sort;

import spec.mcrl2obj.MCRL2;

/**
 * This is the Placeholder class and it represents all the placeholder used in
 * the mCRL2 specification. Every placeholder is identified by a name and by the
 * name of the Sort.
 *
 * @author S. Belluccini
 *
 */
public class Placeholder extends Data {

	private String name;
	private String nameSort;

	/**
	 * Constructor for the placeholder class, it initialize the name of this object
	 * and sets the give sort
	 * 
	 * @param nameSort the name of the sort of this placeholder
	 */
	public Placeholder(String nameSort) {
		this.name = MCRL2.getDataName();
		this.nameSort = nameSort;
	}

	/**
	 * Another constructor for the placeholder class, it sets the name and the nameSort with the given values
	 * @param name the name of the placeholder
	 * @param nameSort the name of the sort of this placeholder
	 */
	public Placeholder(String name, String nameSort) {
		this.name = name;
		this.nameSort = nameSort;
	}

	/**
	 * Returns the name of the placeholder
	 */
	@Override
	public String toString() {
		return this.name;
	}

	
	public String toStringVar() {
		return this.name + ":" + nameSort + ";\n";
	}
	
	
	/**
	 * Returns the sort name of this placeholder
	 * @return
	 */
	public String getNameSort() {
		return this.nameSort;
	}

}
