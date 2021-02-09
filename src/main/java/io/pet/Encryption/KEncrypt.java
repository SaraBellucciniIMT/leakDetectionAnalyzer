package io.pet.Encryption;

import java.util.Set;

import org.jbpt.pm.DataNode;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.DotBPMNKeyW;
import io.pet.AbstractDataPET;
import io.pet.AbstractTaskPET;
import io.pet.PETLabel;
import sort.Data;
import sort.ISort;
import sort.Name;

public class KEncrypt extends AbstractTaskPET {

	private String publickeyId;
	private Cipher outputcipher;

	public KEncrypt() {
	}

	public KEncrypt(String publickey) {
		this.publickeyId = publickey;
	}

	@Override
	public PETLabel getPETLabel() {
		return PETLabel.KENCRYPT;
	}

	/**
	 * Returns the groupid associated to the public key used in this encryption
	 * 
	 * @return
	 */
	public String getGroupId() {
		if(outputcipher== null) {
			this.outputcipher = new Cipher(this.publickeyId);
		}
		return this.outputcipher.getIdPet();
	}

	/**
	 * {@inheritDoc} Not implemented for this class, it always return null;
	 */
	@Override
	public ISort defineInputDataSort(DataNode pn) {
		return null;
	}

	/**
	 * {@inheritDoc} Returns pair(cipher(name),groupid)
	 */
	@Override
	public ISort defineOutputDataSort(DataNode pn) {
		return new Data(new Name(pn.getName()), PETLabel.CIPHER, getGroupId());
	}

	@Override
	public AbstractTaskPET interpreterTask(Element line, Elements lines, Set<DataNode> datanodes) throws JSONException {
		String description = line.getElementsByTag(line.tagName()).text();
		JSONObject obj = new JSONObject(description);
		String publickey = obj.getString(DotBPMNKeyW.KEY.getValue());
		String outputDataId = "";
		for (Element e : lines) {
			if (e.tagName().equals(DotBPMNKeyW.DATAOUTPUTASS.getValue()))
				outputDataId = e.getElementsByTag(DotBPMNKeyW.BPMNTARGETREF.getValue()).text();
		}
		String groupid = null;
		for (DataNode d : datanodes) {
			if (d.getId().equals(publickey)) {
				if (line.tagName().equals(DotBPMNKeyW.SKENCRYPT.getValue())) {
					d.setTag(new PKPrivate(groupid));
				}
				groupid = ((AbstractDataPET)d.getTag()).getGroupId();
				break;
			}
		}
		for (DataNode d : datanodes) {
			if (d.getId().equals(outputDataId))
				d.setTag(new Cipher(groupid));
		}
		return new KEncrypt(groupid);
	}

}
