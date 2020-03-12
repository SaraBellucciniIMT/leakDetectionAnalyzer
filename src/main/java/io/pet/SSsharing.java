/**
 * 
 */
package io.pet;

import spec.mcrl2obj.mCRL2;

/**
 * @author sara
 * it can be 
 */

public class SSsharing extends PET{

	private int threshold = -1;
	
	public SSsharing(int id_shares) {
		this.petname = PETLabel.SSSHARING;
		this.id = id_shares;
	}
	
	
	public void setThreshold(int t) {
		this.threshold = t;
	}
	
	//Id that identify from which ssharing task this share has been created
	public int getThreshold() {
		return threshold;
	}

	@Override
	public String getPNamePET() {
		return mCRL2.sss;
	}

	//Return the treshold 
	@Override
	public int getIdPet() {
		return id;
	}

}
