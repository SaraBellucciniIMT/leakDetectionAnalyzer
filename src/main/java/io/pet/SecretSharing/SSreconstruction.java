package io.pet.SecretSharing;

import java.util.Set;

import org.jbpt.pm.DataNode;
import org.json.JSONException;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.pet.AbstractTaskPET;
import io.pet.PETLabel;
import sort.ISort;

/**
 * This class describes the ssrecostruction stereotype
 * @author S. Belluccini
 *
 */
public class SSreconstruction extends AbstractTaskPET {
	
	
	/**
	 * Constructor for the SSreconstruction class. It initialize an id to be use in the mCRL2 specification if needed
	 * @param id a number that identify this recostruction 
	 */
	public SSreconstruction() {}
	
	public String getThreshold() {
		return SSsharing.THRESHOLD;
	} 

	@Override
	public PETLabel getPETLabel() {
		return PETLabel.SSRECONTRUCTION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractTaskPET interpreterTask(Element line, Elements lines,Set<DataNode> datanodes) throws JSONException {
		return new SSreconstruction();
	}

	/**
	 * {@inheritDoc} Not implemented for this class, it always return null;
	 */
	@Override
	public ISort defineInputDataSort(DataNode pn) {
		return null;
	}

	/**
	 * {@inheritDoc} Not implemented for this class, it always return null;
	 */
	@Override
	public ISort defineOutputDataSort(DataNode pn) {
		return null;
	}

	
}
