package spec.mcrl2obj;

import io.pet.PET;

/**
 * @author sara
 * 
 *         A data parameter ::= triple(name,private,id) | p | eps | true | false
 *         where name is the data name, private= true or private = false if is a
 *         secret or not, id of the partecipant that produced it eps empty data
 */
public class DataParameter {

	private static int index = 0;
	private String data = "";
	// Type of privacy
	private PET pet = null;
	// For temporary parameters
	private static final String placeholderChar = "ph";
	private String placeholder = "";
	private Sort sort;

	// Used to define (false,true value for temporary action)
	public DataParameter(String name, Sort sort) {
		this.data = adjustName(name);
		this.sort = sort;
	}
	
	//
	public DataParameter(Sort sort, String name) {
		this.placeholder = name;
		this.sort = sort;
	}

	// Random data, used especially in sum e1,...,en:Sort
	public DataParameter(Sort sort) {
		this.placeholder = placeholderChar + (index++);
		this.sort = sort;
	}

	public void setPlaceHolder() {
		this.placeholder = placeholderChar + (index++);
	}

	public String getPlaeholder() {
		if (this.placeholder.isEmpty())
			setPlaceHolder();
		return this.placeholder;
	}

	public Sort getSort() {
		return this.sort;
	}

	public String getName() {
		return data;
	}

	public Boolean isPrivate() {
		if (pet != null)
			return true;
		return false;
	}

	public void setPrivate(PET p) {
		this.pet = p;
	}

	private String adjustName(String s) {
		if (s.contains(" "))
			return s.replace(" ", "_");
		return s;
	}

	@Override
	public String toString() {
		if(sort.equals(mCRL2.sortbool)) {
			if(!data.isEmpty())
				return data;
			return placeholder;
		}
		if(data.equals(mCRL2.eps))
			return mCRL2.eps;
		if(pet != null) {
			return "pnode(pair(" + pet.getPNamePET() +"(" + data + ")," + pet.getIdPet() +"))";
		}else if(!data.isEmpty())
			return "node("+data+")";
		else if(!placeholder.isEmpty())
			return placeholder;
		else
			return null;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
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
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
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
