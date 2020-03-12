package io.pet;

public abstract class PET {

	protected PETLabel petname;
	protected int id;
	
	public abstract String getPNamePET();
	public abstract int getIdPet();
	
	public PETLabel getPET() {
		return petname;
	}

	public boolean isempty() {
		if (this.petname == null)
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		if (petname != other.petname)
			return false;
		if((getClass().equals(SScomputation.class))) {
			if(!((SScomputation)this).getGroupId().equals(((SScomputation)obj).getGroupId()))
					return false;
		}
		return true;
	}
}
