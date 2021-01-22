package io.pet.Encryption;

import java.util.Set;

import org.jbpt.pm.DataNode;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import io.DotBPMNKeyW;
import io.pet.AbstractTaskPET;
import io.pet.PETLabel;
import sort.ISort;

public class KDecrypt extends AbstractTaskPET {

	private DataNode privatekey;

	public KDecrypt() {
	}

	public KDecrypt(DataNode privatekey) {
		this.privatekey = privatekey;
	}

	@Override
	public PETLabel getPETLabel() {
		return PETLabel.KDECRYPT;
	}

	/**
	 * Returns the groupid associated to the private key of this deconding key
	 * 
	 * @return
	 */
	public String getGroupId() {
		return ((PKPrivate) privatekey.getTag()).getGroupId();
	}

	/**
	 * {@inheritDoc} Not implemented for this class
	 */
	@Override
	public ISort defineInputDataSort(DataNode pn) {
		return null;
	}

	/**
	 * {@inheritDoc} Not implemented for this class
	 */
	@Override
	public ISort defineOutputDataSort(DataNode pn) {
		return null;
	}

	@Override
	public AbstractTaskPET interpreterTask(Element xmlline, Elements xmllines, Set<DataNode> datanodes)
			throws JSONException {
		String description = xmlline.getElementsByTag(xmlline.tagName()).text();
		JSONObject obj = new JSONObject(description);
		String privatekey = obj.getString(DotBPMNKeyW.KEY.getValue());
		for (DataNode d : datanodes) {
			if (d.getId().equals(privatekey)) {
				return new KDecrypt(d);
			}
		}
		return null;
	}

}
