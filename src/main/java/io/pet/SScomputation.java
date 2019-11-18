/**
 * 
 */
package io.pet;

/**
 * @author sara
 * 
 */
public class SScomputation extends PET{

	private String group_id;
	public SScomputation(String group_id) {
		this.petname = PETLabel.SSCOMPUTATION;
		this.group_id = group_id;
		setID_protection();
	}
	
	public void setTreshold(int i) {
		if(this.treshold ==-1 || this.treshold>i)
		this.treshold = i;
	}
	
	public String getGroup_id() {
		return this.group_id;
	}

	
}
