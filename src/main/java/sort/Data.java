package sort;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import io.pet.PETLabel;
import spec.mcrl2obj.MCRL2Utils;

/**
 * The Data sort class that defines a data object has a privacy data or not
 * based on pet existent
 * 
 * @author S. Belluccini
 */
public class Data implements ISort {

	private Name name;
	private Privacy privacy;
	private final static String eps = "eps";
	private final static String NULLVAR = "vnull";

	public Data() {
		this.name = null;
		this.privacy = null;
	}

	public Data(Name n, PETLabel petLabel, String id) {
		this.privacy = new Privacy(n, petLabel, id);
	}

	public Data(Name n) {
		this.name = n;
	}

	public static Data eps() {
		return new Data(new Name(eps, eps));
	}
	
	public static Data nullvar() {
		return new Data(new Name(NULLVAR,NULLVAR));
	}

	public PETLabel getStereotype() {
		if(this.privacy!= null)
			return this.privacy.getStereotype();
		else
			return null;
	}
	/**
	 * Checks if this name data correspond to the name of this data type object
	 * 
	 * @param nameData the name to compare
	 * @return true if this name data correspond to the name of this data type
	 *         object, false otherwise
	 */
	public boolean equalName(String nameData) {
		if (this.name != null && this.name.getRealName().equals(nameData))
			return true;
		else if (this.privacy != null && this.privacy.getName().getRealName().equals(nameData)) {
			return true;
		}
		return false;
	}

	public String getRealName() {
		if (this.name != null)
			return this.name.getRealName();
		else if (this.privacy != null) {
			return this.privacy.getName().getRealName();
		}
		return null;
	}

	/**
	 * Returns the id that is the name of this data in the specification
	 * 
	 * @return the id that is the name of this data in the specification
	 */
	public String getId() {
		if (this.name != null)
			return this.name.getId();
		else if (this.privacy != null) {
			return this.privacy.getName().getId();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		if (name != null) {
			if (name.getId().equals(eps))
				return eps;
			else if(name.getId().equals(NULLVAR))
				return NULLVAR;
			return MCRL2Utils.node + "(" + name.toString() + ")";
		} else if (privacy != null)
			return MCRL2Utils.pnode + "(" + privacy.toString() + ")";
		else
			return "";
	}

	/**
	 * Prints Data = struct node(value :Name)?is_node | pnode(pvalue
	 * :Privacy)?is_pnode|eps; {@inheritDoc}
	 */
	public String toStringSort() {
		if (!Privacy.setPname.isEmpty()) {
			return nameSort() + " = struct " + MCRL2Utils.node + "(" + MCRL2Utils.v + ":" + Name.nameSort() + ")?"
					+ MCRL2Utils.is_n + "|" + MCRL2Utils.pnode + "(" + MCRL2Utils.pv + ":" + Privacy.nameSort() + ")?"
					+ MCRL2Utils.is_pn + "|" + eps + "|" + NULLVAR+ ";";
		} else
			return nameSort() + " = struct " + MCRL2Utils.node + "(" + MCRL2Utils.v + ":" + Name.nameSort() + ")?"
					+ MCRL2Utils.is_n + "|" + eps + "|"+ NULLVAR + ";";

	}

	public static String nameSort() {
		return "Data";
	}

	@Override
	public String getNameSort() {
		return Data.nameSort();
	}

	public static Data detectData(String s, Set<Data> mcrl2data) {
		s= s.replaceAll(" ", "");
		if (s.equals(eps))
			return eps();
		else if(s.equals(NULLVAR))
			return nullvar();
		String named = null;
		Pattern pn = Pattern.compile(MCRL2Utils.pnode + "\\((.*)\\)");
		Matcher m = pn.matcher(s);
		if (m.find()) {
			String d = m.group(1);
			Pattern pv = Pattern.compile(MCRL2Utils.pair + "\\((.*)\\)");
			Matcher m1 = pv.matcher(d);
			if (m1.find()) {
				String pvd = m1.group(1);
				for (PETLabel pet : PETLabel.values()) {
					Pattern petpat = Pattern.compile(pet.getValue() + "\\((.*)\\)\\,(.*)");
					Matcher m2 = petpat.matcher(pvd);
					if (m2.find()) {
						named = m2.group(1);
						break;
					}
				}
			}
		} else {
			pn = Pattern.compile(MCRL2Utils.node + "\\((.*)\\)");
			m = pn.matcher(s);
			if (m.find()) {
				named = m.group(1);
			}
		}
		for (Data data : mcrl2data) {
			if (data.getId().equals(named))
				return data;
		}
		return null;

	}

}
