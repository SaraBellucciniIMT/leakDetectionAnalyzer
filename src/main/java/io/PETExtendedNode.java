/**
 * 
 */
package io;

import java.util.ArrayList;
import java.util.List;

import org.jbpt.pm.DataNode;

import io.pet.PET;

/**
 * @author sara
 *
 */
public class PETExtendedNode extends DataNode{

	private List<PET> pet;
	
	public PETExtendedNode() {
		this.pet = new ArrayList<PET>();
	}
	
	public List<PET> getPET() {
		return this.pet;
	}
	
	public void setPET(PET pet) {
		this.pet.add(pet);
	}
	
	public boolean hasPET() {
		if(pet.isEmpty())
			return false;
		return true;
	}
	
}
