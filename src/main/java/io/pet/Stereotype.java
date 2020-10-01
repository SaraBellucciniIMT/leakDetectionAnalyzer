package io.pet;

public class Stereotype {

	private PETLabel label;
	public Stereotype(PETLabel label) {
		this.label = label;
	}
	
	public String getFunction() {
		return "is_"+label.toString();
	}
	
	public PETLabel getPETLabel() {
		return this.label;
	}

}
