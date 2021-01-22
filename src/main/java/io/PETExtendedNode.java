/**
 * 
 */
package io;

import java.util.HashSet;
import java.util.Set;

import org.jbpt.pm.DataNode;


/**
 * This is the PET extended node class that defines a possibility for data nodes
 * to have a pet associated to it.
 * 
 * @author S. Belluccini
 *
 */
public class PETExtendedNode extends DataNode {
	//private Set<String> objRef;
	//private String objRef;

	/**
	 * Constructor for the PETExdendedNode class
	 */
	/*public PETExtendedNode() {
		//this.objRef = new HashSet<String>();
	}*/

	/**
	 * Adds the object reference to this data node
	 * @param ref the object reference
	 */
	/*public void addDataObjectReference(String ref) {
		this.objRef.add(ref);
	}*/
	
	/**
	 * Returns true if the object reference is one the the objcet references in the node
	 * @param ref the object reference
	 * @return true if the object reference is one the the objcet references in the node, false otherwise
	 */
	/*public boolean containsObjRef(String ref) {
		if(this.objRef.contains(ref))
			return true;
		return false;
	}*/
	
	
	/**
	 * Returns the associated PET if exists, otherwise null
	 * 
	 * @return the associated PET if exists, otherwise null
	 */
	/*public AbstractTaskPET getPET() {
		if (hasPET())
			return this.pet;
		else
			return null;
	}*/
	
	/**
	 * Sets the PET associated to this node
	 * @param pet associated to this node
	 */
	/*public void setPET(AbstractTaskPET pet) {
		this.pet = pet;
	}*/

	/**
	 * Checks if this node has a PET associated or not
	 * @return true, if a pet associated exists, false otherwise
	 */
	/*public boolean hasPET() {
		if (pet == null)
			return false;
		return true;
	}*/

}
