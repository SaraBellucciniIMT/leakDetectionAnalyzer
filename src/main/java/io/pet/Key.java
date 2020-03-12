package io.pet;

import spec.mcrl2obj.mCRL2;

public class Key extends PET {

	
	public Key() {
		this.petname = PETLabel.KEY;
	}

	@Override
	public String getPNamePET() {
		// TODO Auto-generated method stub
		return mCRL2.dk;
	}
	
	//Should be the same for public and private key pair 
	public void setIdPet(int id) {
		this.id = id;
	}
	
	@Override
	public int getIdPet() {
		// TODO Auto-generated method stub
		return id;
	}

}
