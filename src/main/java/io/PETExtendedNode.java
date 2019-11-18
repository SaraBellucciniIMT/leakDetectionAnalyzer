/**
 * 
 */
package io;

import org.jbpt.pm.DataNode;
import io.pet.PET;

/**
 * @author sara
 *
 */
public class PETExtendedNode extends DataNode{

	private PET pet;
	
	public PETExtendedNode() {
		this.pet = null;
	}
	
	public PET getPET() {
		if(hasPET())
		 return this.pet;
		else
			return null;
	}
	
	public void setPET(PET pet) {
		this.pet = pet;
	}
	
	public boolean hasPET() {
		if(pet == null)
			return false;
		return true;
	}
	
}
