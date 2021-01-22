package io.pet;
import java.util.Set;

import org.jbpt.pm.DataNode;
import org.json.JSONException;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import rpstTest.Utils;
import sort.ISort;

/**
 * Abstract class representing PETs objects
 * 
 * @see #getPNamePET()
 * @see #getIdPet()
 * @see #getPET()
 * 
 * @author S. Bellucini
 *
 */
public abstract class AbstractTaskPET implements IPET{

	private String id;
	
	/**
	 * Returns the id that identifies this AbstractTaskPET inside the mCRL2 specification
	 * @return the id that identifies this AbstractTaskPET inside the mCRL2 specification
	 */
	public String getIdPet() {
		if(this.id == null)
			this.id = String.valueOf(Utils.getId());
		return this.id;
	}

	
	/**
	 * Returns the ISort or type that the data in input of this pet object should have
	 * @param pn the data object
	 * @return the ISort or type that the data in input of this pet object should have, if is not specified it returns null
	 */
	public abstract ISort defineInputDataSort(DataNode pn);
	
	/**
	 * Returns the ISort or type that the data in output of this pet object should have
	 * @param pn the data object
	 * @return the ISort or type that the data in output of this pet object should have, if is not specified it returns null
	 */
	public abstract ISort defineOutputDataSort(DataNode pn);
	
	/**
	 * Interpreter for the BPMN file. It returns the AbstractTaskPET associated to a task, extracted from the xmllines
	 * @param xmlline the line that has as tag the correspondent PETLabel
	 * @param xmllines the lines describing the task
	 * @param datanodes the set of datanodes extracted from the BPMN file
	 * @return  the AbstractTaskPET extracted from the xmllines
	 * @throws JSONException if it cannot read the Json correctly
	 */
	public abstract AbstractTaskPET interpreterTask(Element xmlline, Elements xmllines, Set<DataNode> datanodes) throws JSONException;

}
