/**
 * 
 */
package spec.mcrl2obj;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

import io.pet.PET;

/**
 * @author sara
 *
 */
public class StructSort extends Sort {

	private Set<Pair<String,PET>> pair = new HashSet<Pair<String, PET>>();

	public StructSort(String name) {
		super(name);
	}

	@Override
	public String toString() {
		String s = this.getName() + " = struct ";
		int i = 0;
		for (String type : this.getTypes()) {
			if (type.contains(" "))
				type = type.replace(" ", "_");
			s = s + type;
			if (i != this.getTypes().size() - 1)
				s = s + "|";
			i++;
		}
		return s;
	}

	public void addPair(Pair<String, PET> p) {
		this.pair.add(p);
	}

	public Set<Pair<String,PET>> getPrivatePair() {
		return pair;
	}

}
