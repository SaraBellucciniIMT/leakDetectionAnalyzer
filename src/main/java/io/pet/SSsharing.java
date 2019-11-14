/**
 * 
 */
package io.pet;

/**
 * @author sara
 * it can be 
 */

public class SSsharing extends PET{

	private int treshold;
	
	public SSsharing(int treshold) {
		this.petname = PETLabel.SSSHARING;
		this.treshold = treshold;
		//this.computation = computation;
				
	}
	@Override
	public PETLabel getPET() {
		return this.petname;
	}
	
	public int getTreshold() {
		return this.treshold;
	}

}
