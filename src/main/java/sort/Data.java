package sort;

import java.util.HashSet;
/*
 * Data define the abstract sort data for data parameters, it can be:
 * -a data of type name
 * -a data of type privacy
 * -eps data,i.e. empty value
 */

public class Data extends AbstractStructSort {

	public static final String eps = "eps";
	private Name node_value;
	private Privacy pnode_value;
	public static final String node = "node";
	public static final String pnode = "pnode";
	public static final String is_n = "is_node";
	public static final String is_pn = "is_pnode";

	public Data(Name node) {
		this.node_value = node;
	}

	public Data(Privacy pnode) {
		this.pnode_value = pnode;
	}

	public Name value() {
		return node_value;
	}

	public Privacy pvalue() {
		return pnode_value;
	}

	@Override
	public String toString() {
		if (node_value != null)
			return node + node_value.toString();
		else if (pnode_value != null)
			return pnode + pnode_value.toString();
		else
			return null;
	}

	@Override
	public String getDefinition() {
		this.type = new HashSet<String>();
		this.type.add(node + "( value:" + Name.class.getName() + ")?" + is_n);
		this.type.add(pnode + "( pvalue:" + Privacy.class.getName() + ")?" + is_pn);
		this.type.add(eps);
		return this.getClass().getName() + printStruct();
	}

}
