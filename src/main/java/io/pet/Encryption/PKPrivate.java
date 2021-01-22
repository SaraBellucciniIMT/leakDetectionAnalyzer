package io.pet.Encryption;

import org.jbpt.pm.DataNode;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.DotBPMNKeyW;
import io.pet.AbstractDataPET;
import io.pet.IPET;
import io.pet.PETLabel;
import sort.Data;
import sort.ISort;
import sort.Name;

public class PKPrivate extends AbstractDataPET {
	
	public PKPrivate(String groupId) {
		this.groupId =String.valueOf(IPET.setGroupId(groupId));
	}

	public PKPrivate() {
	} 
	
	@Override
	public PETLabel getPETLabel() {
		return PETLabel.PRIVATEKEY;
	}

	@Override
	public AbstractDataPET interpreterData(Element line, Elements xmllines) throws JSONException {
		String description = line.getElementsByTag(line.tagName()).text();
		JSONObject obj = new JSONObject(description);
		String groupid = (String) obj.get(DotBPMNKeyW.GROUPID.getValue());
		return new PKPrivate(groupid);
	}

	/**
	 * {@inheritDoc} Returns pnode(pair(decodingkey,id))
	 */
	@Override
	public ISort printData(DataNode pn) {
		return new Data(new Name(pn.getName()), PETLabel.DECODINGKEY, getGroupId());
	}

}
