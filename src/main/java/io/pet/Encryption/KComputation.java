package io.pet.Encryption;

import java.util.Set;

import org.jbpt.pm.DataNode;
import org.json.JSONArray;
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

public class KComputation extends AbstractTaskPET {

	public DataNode cipherobj;

	public KComputation() {}
	public KComputation(DataNode id) {
		this.cipherobj = id;
	}

	@Override
	public PETLabel getPETLabel() {
		return PETLabel.KCOMPUTATION;
	}
	
	public String getGroupId() {
		return ((Cipher)cipherobj.getTag()).getGroupId();
	}
	@Override
	public ISort defineInputDataSort(DataNode pn) {
		return null;
	}

	@Override
	public ISort defineOutputDataSort(DataNode pn) {
		return new Data(new Name(pn.getName()), PETLabel.CIPHER, getGroupId());
	}

	@Override
	public AbstractTaskPET interpreterTask(Element line, Elements lines, Set<DataNode> datanodes) throws JSONException {
		String description = line.getElementsByTag(line.tagName()).text();
		JSONObject obj = new JSONObject(description);
		JSONArray arr = obj.getJSONArray(DotBPMNKeyW.INPUTTYPES.getValue());
		for (int i = 0; i < arr.length(); i++) {
			JSONObject inputobj = arr.getJSONObject(i);
			String id = null;
			if (inputobj.getString(DotBPMNKeyW.TYPE.getValue()).equals(DotBPMNKeyW.ENCRYPTED.getValue()))
				id = inputobj.getString(DotBPMNKeyW.ID.getValue());

			for (DataNode d : datanodes) {
				if (d.getId().equals(id))
					return new KComputation(d);

			}

		}

		return null;
	}

}
