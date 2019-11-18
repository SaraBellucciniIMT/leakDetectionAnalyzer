package io.pet;

import rpstTest.Utils;

public abstract class PET {

	protected PETLabel petname;
	protected int id_protection = -1;
	protected int treshold=-1;
	
	public PETLabel getPET() {
		return petname;
	}
	
	public boolean isempty() {
		if(this.petname == null)
			return true;
		return false;
	}
	
	protected void setID_protection() {
		id_protection = Utils.getId();
	}
	
	public int getID_protection() {
		return id_protection;
	}

	public int getTreshold() {
		return this.treshold;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id_protection;
		result = prime * result + ((petname == null) ? 0 : petname.hashCode());
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
		PET other = (PET) obj;
		if (id_protection != other.id_protection)
			return false;
		if (petname != other.petname)
			return false;
		return true;
	}
}
