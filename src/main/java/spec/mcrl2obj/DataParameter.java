package spec.mcrl2obj;

public class DataParameter {
	
	
	private static int index =0;
	private String name;
	//For temporary parameters
	private static final String parameter = "p";
	private static final String placeholderChar = "e";
	private String placeholder;
	
	/*
	 * Fixed data parameter
	 */
	public DataParameter(String name) {
		this.name = name;
	}
	//Generate temporary parameters name
	public DataParameter() {
		 this.name = parameter + (index++);
	}
	
	
	
	public void setPlaceHolder() {
		this.placeholder = placeholderChar + (index++);
	}
	
	public String getPlaeholder() {
		return this.placeholder;
	}
	public static DataParameter setEmptyParameter() {
		 return new DataParameter();
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
		return name ;
	}
	
}
