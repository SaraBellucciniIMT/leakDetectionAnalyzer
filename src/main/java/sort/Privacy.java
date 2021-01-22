package sort;

import java.util.HashSet;
import java.util.Set;

import io.pet.PETLabel;
import rpstTest.Utils;
import spec.mcrl2obj.MCRL2Utils;

/**
 * Class representing data objects enhanced with privacy features Privacy and
 * Pname sort among the sorts defined in the mCRL2 specification
 * 
 * @author S. Belluccini
 *
 */
public class Privacy implements ISort {

	public static Set<PETLabel> setPname = new HashSet<PETLabel>();
	/**
	 * The data object
	 */
	private Name data;
	/**
	 * The stereotype of the data object
	 */
	private PETLabel stereotype;
	/**
	 * The id that group this data objects with other data objects
	 */
	private String id;

	public Privacy() {
		this.data = null;
		this.stereotype = null;
	}

	/**
	 * Constructor for the class Privacy
	 * 
	 * @param data       the data object
	 * @param stereotype the privacy features connect to this data object
	 * @param id         the id that groups this data object with other data objects
	 */
	public Privacy(Name data, PETLabel stereotype, String id) {
		this.data = data;
		this.stereotype = stereotype;
		this.id = id;
		setPname.add(stereotype);

	}

	/**
	 * Returns the value of the name type of this privacy data object
	 * 
	 * @return the value of the name type of this privacy data object
	 */
	public Name getName() {
		return this.data;
	}

	/**
	 * Prints a data objects as: pair("stereotype("data")","id")
	 */
	public String toString() {
		if (stereotype != null && data != null)
			return "pair(" + stereotype.getValue() + "(" + data.toString() + ")," + id + ")";
		else
			return null;

	}

	public String toStringSort() {
		if (!setPname.isEmpty()) {
			String[] pnamearray = new String[setPname.size()];
			int i = 0;
			for (PETLabel l : setPname) {
				pnamearray[i++] = l.getValue() + "(" + Name.nameSort() + ")?" + l.is_value();
			}
			String pname = Utils.printAsStruct("PName", pnamearray) + "\n";

			String privacy = nameSort() + " = struct " + MCRL2Utils.pair + "(" + MCRL2Utils.frt + ":PName,"
					+ MCRL2Utils.snd + ":" + ISort.NAT + ");";
			return pname + privacy;
		} else
			return "";
	}

	public static String nameSort() {
		return "Privacy";
	}

	@Override
	public String getNameSort() {
		return nameSort();
	}

}
