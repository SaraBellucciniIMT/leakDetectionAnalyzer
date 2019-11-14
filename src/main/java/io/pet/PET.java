package io.pet;

public abstract class PET {

	protected PETLabel petname;
	
	public abstract PETLabel getPET();
	
	public boolean isempty() {
		if(this.petname == null)
			return true;
		return false;
	}
		
}
