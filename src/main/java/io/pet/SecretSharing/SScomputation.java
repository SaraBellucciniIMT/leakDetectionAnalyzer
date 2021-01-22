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
import io.pet.IPET;
import io.pet.PETLabel;
import sort.Data;
import sort.ISort;
import sort.Name;

/**
 * This class describes the SSComputation/addSSComputation/FunSSComputation stereotypes
 * 
 * @see #group_id the group id name of this computation
 * 
 * @author S. Belluccini
 * 
 */
public class SScomputation extends AbstractTaskPET {

	private String group_id;

	public SScomputation() {};

	/**
	 * Constructor for the SScomputation class.
	 * 
	 * @param group_id the group id that identifies this computation
	 */
	private SScomputation(String group_id) {
		this.group_id = group_id;
	}

	public String getThreshold() {
		return SSsharing.THRESHOLD;
	}

	/**
	 * Returns the group id of this computation
	 * 
	 * @return the group id of this computation
	 */
	public String getGroupId() {
		return this.group_id;
	}

	@Override
	public PETLabel getPETLabel() {
		return PETLabel.SSCOMPUTATION;
	}

	@Override
	public AbstractTaskPET interpreterTask(Element line, Elements lines,Set<DataNode> datanodes) throws JSONException {
		String description = line.getElementsByTag(line.tagName()).text();
		JSONObject obj = new JSONObject(description);
		String groupID = String.valueOf(IPET.setGroupId(obj.getString(DotBPMNKeyW.GROUPID.getValue())));
		SScomputation computation = new SScomputation(groupID);
		/*JSONArray arr = obj.getJSONArray(DotBPMNKeyW.INPUTS.getValue());
		for (int i = 0; i < arr.length(); i++) {
			JSONObject inputobj = arr.getJSONObject(i);
			JSONArray inputarr = inputobj.getJSONArray(DotBPMNKeyW.INPUTS.getValue());
			for (int j = 0; j < inputarr.length(); j++) {
				JSONObject objname = inputarr.getJSONObject(j);
				computation.addInputsData(objname.getString(DotBPMNKeyW.ID.getValue()));
			}

		}*/
		return computation;
	}

	/**
	 * {@inheritDoc} Not implemented for this class, it always return null;
	 */
	@Override
	public ISort defineInputDataSort(DataNode pn) {
		return null;
	}

	@Override
	public ISort defineOutputDataSort(DataNode pn) {
		return new Data(new Name(pn.getName()), PETLabel.SSCOMPUTATION, group_id);
	}

}
