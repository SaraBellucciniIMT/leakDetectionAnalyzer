package sort;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import io.pet.PETLabel;

/*
 * PName is the class representing the definition of the PName sort in the specification
 * map : map the pname values to a set of PETLabel defined in the xes standard in order to identify PETs
 * name: is the element having the type
 * type: is the type of the name
 */

public class PName extends AbstractStructSort {

	private Map<PNameType, Set<PETLabel>> map;
	private Name name;
	private PNameType pnametype;
	
	public PName(Pair<PNameType, Set<PETLabel>>... pname) {
		for (int i = 0; i < pname.length; i++)
			map.put(pname[i].getLeft(), pname[i].getRight());
	}

	public PName(Name n, PNameType t) {
		this.name = n;
		this.pnametype= t;
	}
	
	@Override
	public String getDefinition() {
		for (PNameType k : map.keySet())
			this.type.add(k + "(" + Name.class.getName() + ")" + k.getValue());
		return "PName" + printStruct();
	}

	@Override
	public String toString() {
		return pnametype.name()+"("+name.toString()+")";
	}
	
}
