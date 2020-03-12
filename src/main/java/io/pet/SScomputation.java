/**
 * 
 */
package io.pet;

import rpstTest.Utils;
import spec.mcrl2obj.mCRL2;

/**
 * @author sara
 * 
 */
public class SScomputation extends PET{

	private String group_id;
	public SScomputation(String group_id) {
		this.petname = PETLabel.SSCOMPUTATION;
		this.group_id = group_id;
		this.id = Utils.getId();
		
	}

	@Override
	public String getPNamePET() {
		// TODO Auto-generated method stub
		return mCRL2.ssc;
	}
	public String getGroupId() {
		return this.group_id;
	}
	
	//Return the group id of the computation
	@Override
	//We need an int because the funcion to check violation in mcrl2 takes int 
	public int getIdPet() {
		// TODO Auto-generated method stub
		return id;
	}

	
}
