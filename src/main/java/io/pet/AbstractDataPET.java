package io.pet;

import org.jbpt.pm.DataNode;
import org.json.JSONException;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sort.ISort;

public abstract class AbstractDataPET implements IPET {

	protected String groupId;
	/**
	 * Interpreter for the BPMN file. It returns the AbstractTaskPET associated to 
	 * @param xmlline
	 * @param xmllines
	 * @return
	 * @throws JSONException
	 */
	public abstract AbstractDataPET interpreterData(Element xmlline, Elements xmllines) throws JSONException; 
	
	public abstract ISort printData(DataNode pn);
	
	public String getGroupId() {
		return this.groupId;
	}

}
