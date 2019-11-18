package spec.mcrl2obj;

import java.util.HashSet;
import java.util.Set;

public class Sort {

	private String name;
	private Set<String> types = new HashSet<String>();

	public Sort(String name) {
		this.name = name;
	}

	public void addType(String... type) {
		for(int i=0; i<type.length; i++)
			this.types.add(type[i]);

	}

	public Set<String> getTypes() {
		return this.types;
	}

	public boolean containtType(String t) {
		if(types.contains(t))
			return true;
		return false;
	}
	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		String s = getName() + " = ";
		for (String t : types) {
			s = s + t;
		}
		return s;
	}
	
	public boolean isEmpty() {
		if(types.isEmpty())
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sort other = (Sort) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
