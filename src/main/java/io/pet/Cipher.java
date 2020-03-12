package io.pet;
import spec.mcrl2obj.mCRL2;

public class Cipher extends PET{

	public Cipher() {
		this.petname = PETLabel.CIPHER;
	}

	@Override
	public String getPNamePET() {
		// TODO Auto-generated method stub
		return mCRL2.cip;
	}
	
	//Should be equal to the id of the public key used to make the encryption
	public void setIdPet(int id) {
		this.id = id;
	}

	@Override
	public int getIdPet() {
		// TODO Auto-generated method stub
		return id;
	}

}
