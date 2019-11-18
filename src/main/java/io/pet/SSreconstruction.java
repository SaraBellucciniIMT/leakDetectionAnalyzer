package io.pet;

public class SSreconstruction extends PET {

	public SSreconstruction() {
		this.petname = PETLabel.SSRECONTRUCTION;
		setID_protection();
	}
	
	public void setTreshold(int i) {
		if(this.treshold ==-1 || this.treshold>i)
		this.treshold = i;
	}
	

	
}
