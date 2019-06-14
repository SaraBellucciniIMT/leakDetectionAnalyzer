/**
 * 
 */
package io.pet;

/**
 * @author sara
 *
 */
public class SSsharing extends PET{

	private int treshold;
	private int computation;
	
	public SSsharing(int treshold,int computation) {
		this.petname = PETLabel.SSSHARING;
		this.treshold = treshold;
		this.computation = computation;
				
	}
	@Override
	public PETLabel getPET() {
		return this.petname;
	}
	
	public int getTreshold() {
		return this.treshold;
	}
	
	public int getComputation() {
		return this.computation;
	}

}
