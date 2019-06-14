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
		return this.pet;
	}
	
	public void setPET(PET pet) {
		this.pet = pet;
	}
	
}
