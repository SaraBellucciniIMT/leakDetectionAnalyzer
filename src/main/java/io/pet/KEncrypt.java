package io.pet;


public class KEncrypt extends PET {

	private Key key = null;
	
	public KEncrypt() {
		this.petname = PETLabel.KENCRYPT;
		
	}

	@Override
	public String getPNamePET() {
		return null;
	}

	public void setKey(Key key) {
		this.key = key;
		this.id = key.getIdPet();
	}


	public Key getKey() {
		return this.key;
	}

	/*
	 * public String getCipher() { return this.cipher; }
	 */
	@Override
	public int getIdPet() {
		return id;
	}

}
