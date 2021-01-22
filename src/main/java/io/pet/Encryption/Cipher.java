package io.pet.Encryption;

import org.jbpt.pm.DataNode;
import org.json.JSONException;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.pet.AbstractDataPET;
import io.pet.PETLabel;
import sort.ISort;

/**
 * This is the Cipher class. It represents a dataobject in a BPMN file that is as output of an PKEncrypt or PKComputatin task.
 * The groupId indicates which pair of key can be use to decrypt it.
 * @author S. Belluccini
 *
 */
public class Cipher extends AbstractDataPET{
	
	/**
	 * Constructor for the Cipher class. It takes the groupId as input, i.e. the id of the pair of key to decrypt this data
	 * @param groupId the id of the pair of key to decrypt this data
	 */
	public Cipher(String groupId) {
		this.groupId = groupId;
	}
	
	/**
	 * {@inheritDoc} Returns the PETLabel corresponding to this class, i.e. CIPHER
	 */
	@Override
	public PETLabel getPETLabel() {
		return PETLabel.CIPHER;
	}

	/**
	 * {@inheritDoc} Not implemented for this class, it always return null;
	 */
	@Override
	public AbstractDataPET interpreterData(Element xmlline, Elements xmllines) throws JSONException {
		return null;
	}

	/**
	 * {@inheritDoc} Not implemented for this class
	 */
	@Override
	public ISort printData(DataNode pn) {
		return null;
	}

}
