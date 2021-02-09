package io.pet;

import java.util.HashMap;
import java.util.Map;

import rpstTest.Utils;

public interface IPET {
	
	public static Map<String,Integer> group_name = new HashMap<String,Integer>();
	
	/**
	 * Returns the groupid corrisponding to the id
	 * @param id the id given in the BPMN file
	 * @return the groupid corrisponding to the id
	 */
	public static int setGroupId(String id) {
		if(!group_name.containsKey(id))
			group_name.put(id, Utils.getId());
		return group_name.get(id);
	}
	/**
	 * Returns the label that identifies this AbstractTaskPET
	 * @return the label that identifies this AbstractTaskPET
	 */
	public abstract PETLabel getPETLabel();
	
	
	public String getIdPet();
	

}
