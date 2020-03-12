package io.pet;

import spec.mcrl2obj.mCRL2;

public class SSreconstruction extends PET {
	
	//This id is not really useful but we need in the specificaiton because we cannot leave empty the left side of the Privacy pair
	public SSreconstruction(int id) {
		this.petname = PETLabel.SSRECONTRUCTION;
		this.id = id;
	}

	@Override
	public String getPNamePET() {
		// TODO Auto-generated method stub
		return mCRL2.ssr;
	}

	@Override
	public int getIdPet() {
		// TODO Auto-generated method stub
		return this.id;
	}
	
}
