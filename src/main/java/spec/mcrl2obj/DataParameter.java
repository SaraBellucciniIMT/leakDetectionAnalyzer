package spec.mcrl2obj;

public class DataParameter {

	private static int index =0;
	private String name;
	//For temporary parameters
	private static final String placeholderChar = "e";
	private String placeholder;
	private Sort sort;
	/*
	 * Fixed data parameter
	 */
	public DataParameter(String name, Sort sort) {
		this.name = name;
		this.sort = sort;
	}

	public DataParameter(Sort sort) {
		this.name = placeholderChar + (index++);
		this.sort = sort;
	}
	public void setPlaceHolder() {
		this.placeholder = placeholderChar + (index++);
	}
	
	public String getPlaeholder() {
		return this.placeholder;
	}
	
	public Sort getSort() {
		return this.sort;
	}

	/*
	 * Get parameter name
	 */
	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return name ;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((placeholder == null) ? 0 : placeholder.hashCode());
		result = prime * result + ((sort == null) ? 0 : sort.hashCode());
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
		DataParameter other = (DataParameter) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (placeholder == null) {
			if (other.placeholder != null)
				return false;
		} else if (!placeholder.equals(other.placeholder))
			return false;
		if (sort == null) {
			if (other.sort != null)
				return false;
		} else if (!sort.equals(other.sort))
			return false;
		return true;
	}
	
}
