package spec.mcrl2obj;

public class DataParameter {
	
	private Sort sort;
	private static int index =0;
	private String name;
	//For temporary parameters
	private static final String parameter = "p";
	private static final String placeholderChar = "e";
	private String placeholder;
	
	/*
	 * Fixed data parameter
	 */
	public DataParameter(String name,Sort sort) {
		this.name = name;
		this.sort = sort;
	}
	//Generate temporary parameters name
	public DataParameter(Sort sort) {
		 this.name = parameter + (index++);
		 this.sort = sort;
	}
	
	public void setPlaceHolder() {
		this.placeholder = placeholderChar + (index++);
	}
	
	public String getPlaeholder() {
		return this.placeholder;
	}
	public static DataParameter setEmptyParameter(Sort sort) {
		 return new DataParameter(Sort.empty,sort);
	}
	
	/*
	 * Get in which domain it is
	 */
	public Sort getType() {
		return sort;
	}
	
	public static void resetIndex() {
		index =0;
	}
	
	/*
	 * Get parameter name
	 */
	public String getDataParameter() {
		return this.name;
	}

	@Override
	public String toString() {
		return name +":" + sort.getName();
	}
	
}
