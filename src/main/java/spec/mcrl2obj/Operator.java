package spec.mcrl2obj;

public enum Operator {

	DOT("."),
	PLUS("+"),
	PARALLEL("||"),
	SUM("sum"),
	ALLOW("allow"),
	HIDE("hide"),
	COMM("comm"),
	IF("IF");
	
	private final String o;
	
	Operator(String o) {
		this.o = o;
	};
	
	public String getValue() {
		return o;
	}
}
