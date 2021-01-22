
/**
 * 
 */
package io.pet.SecretSharing;

import java.util.Set;

import org.jbpt.pm.DataNode;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.DotBPMNKeyW;
import io.pet.AbstractTaskPET;
import io.pet.PETLabel;
import sort.Data;
import sort.ISort;
import sort.Name;

/**
 * This class describes the SSsharing/AddSSsharing/FunSSsharing pet stereotypes
 * 
 * @see SSsharing#THRESHOLD a number that describe how many shares are needed to recostruct the secret
 * 
 * @author S. Belluccini
 */

public class SSsharing extends AbstractTaskPET {

	protected static String THRESHOLD;

	@Override
	public AbstractTaskPET interpreterTask(Element line, Elements lines, Set<DataNode> datanodes) throws JSONException {
		if (line.tagName().equals(DotBPMNKeyW.SSSHARING.getValue())) {
			String description = line.getElementsByTag(line.tagName()).text();
			JSONObject obj = new JSONObject(description);
			THRESHOLD = String.valueOf(obj.getInt(DotBPMNKeyW.TRESHOLD.getValue()));
		} else if (line.tagName().equals(DotBPMNKeyW.ADDSSSHARING.getValue())) {
			THRESHOLD = String.valueOf(0);
			for (Element e : lines) {
				if (e.tagName().equals(DotBPMNKeyW.DATAOUTPUTASS.getValue()))
					THRESHOLD = String.valueOf(Integer.valueOf(THRESHOLD) + 1);
			}
		} else if (line.tagName().equals(DotBPMNKeyW.FUNSSSHARING.getValue()))
			THRESHOLD = String.valueOf(2);
		return new SSsharing();
	}

	/**
	 * Constructor for the SSsharing object
	 * 
	 * @param treshold the number that indicates how many shares are needed to
	 *                 reconstruct the secret
	 */

	public SSsharing() {
	}

	/**
	 * Returns the threshold to reconstruct this secret, i.e. the minimum number of
	 * shares needed to reconstruct it.
	 * 
	 * @return the threshold to reconstruct this secret, i.e. the minimum number of
	 *         shares needed to reconstruct it.
	 */
	public String getTreshold() {
		return THRESHOLD;
	}

	/**
	 *	{@inheritDoc} Returns the PETLabel corresponding to this class, i.e. SSSHARING
	 */
	@Override
	public PETLabel getPETLabel() {
		return PETLabel.SSSHARING;
	}

	/**
	 * {@inheritDoc} Not implemented for this class, it always return null;
	 */
	@Override
	public ISort defineInputDataSort(DataNode pn) {
		return null;
	}

	/**
	 * {@inheritDoc} Returns an Isort with this form : pnode(sssharing(name),id)
	 */
	@Override
	public ISort defineOutputDataSort(DataNode pn) {
		return new Data(new Name(pn.getName()), PETLabel.SSSHARING, String.valueOf(this.getIdPet()));
	}
}
