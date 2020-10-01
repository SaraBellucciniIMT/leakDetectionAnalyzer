package sort;

public enum PNameType {

	sssharing("is_ssharing"),
	sscomputation("is_sscomputation"),
	ssrecostruction("is_ssrecostruction");
	
	
	private String value;
	
	private PNameType(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}
