/**
 * 
 */
package io.pet;

import java.util.List;

/**
 * @author sara
 *
 */
public class SScomputation extends PET{

	private int groupId ;
	List<String> objref;
	
	public SScomputation(int groupid,List<String> objref) {
		// TODO Auto-generated constructor stub
		this.petname = PETLabel.SSCOMPUTATION;
		this.groupId = groupid;
		this.objref = objref;
	}
	@Override
	public PETLabel getPET() {
		// TODO Auto-generated method stub
		return this.petname;
	}

	public int getGroupId() {
		return this.groupId;
	}
	
	public boolean containObjRef(String s) {
		if(objref.contains(s))
			return true;
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SScomputation other = (SScomputation) obj;
		if (groupId != other.groupId)
			return false;
		return true;
	}
	
}
