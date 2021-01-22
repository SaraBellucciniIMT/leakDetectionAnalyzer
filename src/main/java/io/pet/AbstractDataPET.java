package io.pet;

import org.jbpt.pm.DataNode;
import org.json.JSONException;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sort.ISort;
/**
 * This class is the AbstractDataPET class. It describes variables and methods necessary for PETs associated to data objects.
 * 
 * @see #groupId it's an id that usually identifies objects in the same PET
 * @see #interpreterData(Element, Elements) this method should be implemented to describe how to identify a PET for dataobjects
 * @see #printData(DataNode) this method should be implemented to describe how the data object is printed in the specification
 * 
 * @author S. Belluccini
 *
 */
public abstract class AbstractDataPET implements IPET {

	protected String groupId;
	
	/**
	 * Returns the AbstractDataPET extracted form the given xml lines
	 * @param xmlline the line containing the PET stereotype
	 * @param xmllines all the lines from the PET tag until theend
	 * @return  the AbstractDataPET extracted form the given xml lines
	 * @throws JSONException if it cannot parse the given xml
	 */
	public abstract AbstractDataPET interpreterData(Element xmlline, Elements xmllines) throws JSONException; 
	
	/**
	 * Returns how the data object is printed in the specification
	 * @param pn the data object
	 * @return how the data object is printed in the specification
	 */
	public abstract ISort printData(DataNode pn);
	
	public String getGroupId() {
		return this.groupId;
	}

}
