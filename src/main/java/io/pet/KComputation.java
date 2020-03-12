package io.pet;

public class KComputation extends PET{

	Cipher cipher ;
	public KComputation(int id) {
		this.petname = PETLabel.KCOMPUTATION;
		this.id = id;
	}

	@Override
	public String getPNamePET() {
		return null;
	}

	@Override
	public int getIdPet() {
		return id;
	}
	
	public void setCipher(Cipher c) {
		this.cipher = c;
	}
	
	public int getIdCipher() {
		return this.cipher.getIdPet();
	}
}
