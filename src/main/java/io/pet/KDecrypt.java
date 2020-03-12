package io.pet;

public class KDecrypt extends PET{

	//private String key;
	//private String ciphertext;
	public KDecrypt(int id) {
		this.petname = PETLabel.KDECRYPT;
		this.id = id;
	}

	@Override
	public String getPNamePET() {
		return null;
	}

	/*public void setKeyDecypt(String key) {
		this.key = key;
	}*/
	
	/*public void setCipherText(String ciphertext) {
		this.ciphertext = ciphertext;
	}*/
	
	/*public String getKeyDecypt() {
		return this.key;
	}*/
	
	/*public String getCipherText() {
		return this.ciphertext;
	}*/
	@Override
	public int getIdPet() {
		return id;
	}

}
