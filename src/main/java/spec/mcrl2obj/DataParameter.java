package spec.mcrl2obj;



/**
 * @author sara
 * 
 * A data parameter ::= triple(name,private,id) | p | eps | true | false
 * where name is the data name,  private= true or private = false if is a secret or not, id of the partecipant that produced it
 * eps empty data 
 */
public class DataParameter {

	private static int index =0;
	private String data="";
	private boolean isprivate=false;
	private String id = "";
	//For temporary parameters
	private static final String placeholderChar = "e";
	private String placeholder="";
	private Sort sort;
	
	
	//Used to define (false,true value for temporary action)
	public DataParameter(String name,Sort sort) {
		this.data = adjustName(name);
		this.sort = sort;
	}
	//Random data, used especially in sum e1,...,en:Sort 
	public DataParameter(Sort sort) {
		this.placeholder = placeholderChar + (index++);
		this.sort = sort;
	}
	
	public void setPlaceHolder() {
		this.placeholder = placeholderChar + (index++);
	}
	
	public String getPlaeholder() {
		if(this.placeholder.isEmpty())
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
		return isprivate;
	}
	public void setPrivate() {
		this.isprivate = true;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	private String adjustName(String s) {
		if(s.contains(" "))
			return s.replace(" ", "_");
		return s;
	}
	@Override
	public String toString() {
		if(!this.id.isEmpty())
			return "triple("+data+"," +isprivate+","+ id+")";
		else if(!data.isEmpty())
			return data;
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
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (isprivate ? 1231 : 1237);
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
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (isprivate != other.isprivate)
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
