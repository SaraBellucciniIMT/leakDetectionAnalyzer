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
import sort.ISort;

public class PKPublic extends AbstractDataPET {
	
	public PKPublic(String groupId) {
		this.groupId =String.valueOf(IPET.setGroupId(groupId));
	}

	public PKPublic() {
	}

	@Override
	public PETLabel getPETLabel() {
		return PETLabel.PUBLICKEY;
	}

	@Override
	public AbstractDataPET interpreterData(Element line, Elements lines) throws JSONException {
		String description = line.getElementsByTag(line.tagName()).text();
		JSONObject obj = new JSONObject(description);
		String groupid = (String) obj.get(DotBPMNKeyW.GROUPID.getValue());
		return new PKPublic(groupid);

	}

	/**
	 * {@inheritDoc} Not implemented for this class
	 */
	@Override
	public ISort printData(DataNode pn) {
		 return null;
	}
	

}
