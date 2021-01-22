package io.pet;

import java.util.Set;

import org.jbpt.pm.DataNode;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.DotBPMNKeyW;
import sort.ISort;

public class MPC extends AbstractTaskPET {

	private String groupid;
	
	public MPC() {}
	
	public MPC(String groupid) {
		this.groupid = String.valueOf(IPET.setGroupId(groupid));
	}
	@Override
	public PETLabel getPETLabel() {
		return PETLabel.MPC;
	}
	
	/**
	 * Returns the groupid of this MPC
	 * @return the groupid of this MPC
	 */
	public String getGroupId() {
		return this.groupid;
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
		String groupId = String.valueOf(obj.get(DotBPMNKeyW.GROUPID.getValue()));
		return new MPC(groupId);
	}

}
