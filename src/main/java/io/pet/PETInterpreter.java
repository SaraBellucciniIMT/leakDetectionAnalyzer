package io.pet;

import java.util.Set;

import org.jbpt.pm.DataNode;
import org.json.JSONException;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.DotBPMNKeyW;
import io.pet.Encryption.KComputation;
import io.pet.Encryption.KDecrypt;
import io.pet.Encryption.KEncrypt;
import io.pet.Encryption.PKPrivate;
import io.pet.Encryption.PKPublic;
import io.pet.SecretSharing.SScomputation;
import io.pet.SecretSharing.SSreconstruction;
import io.pet.SecretSharing.SSsharing;

/**
 * Class PETInterpreter defines a way to extract PETs property over xmllines of
 * code
 * 
 * @author S. Belluccini
 *
 */
public class PETInterpreter {

	public static AbstractTaskPET detectPETTask(Elements xmllines, Set<DataNode> datanodes) throws JSONException {
		for (Element x : xmllines) {
			String tagName = x.tagName();
			if (tagName.equals(DotBPMNKeyW.SSSHARING.toString()) || tagName.equals(DotBPMNKeyW.ADDSSSHARING.getValue())
					|| tagName.equals(DotBPMNKeyW.FUNSSSHARING.getValue())) {
				return new SSsharing().interpreterTask(x, xmllines, datanodes);
			} else if (tagName.equals(DotBPMNKeyW.SSCOMPUTATION.toString())
					|| tagName.equals(DotBPMNKeyW.ADDSSCOMPUTATION.getValue())
					|| tagName.equals(DotBPMNKeyW.FUNSSCOMPUTATION.getValue())) {
				return new SScomputation().interpreterTask(x, xmllines, datanodes);
			} else if (tagName.equals(DotBPMNKeyW.SSRECONSTRUCTION.toString())
					|| tagName.equals(DotBPMNKeyW.ADDSSRECONSTRUCTION.getValue())
					|| tagName.equals(DotBPMNKeyW.FUNSSRECONSTRUCTION.getValue())) {
				return new SSreconstruction().interpreterTask(x, xmllines, datanodes);
			}else if(tagName.equals(DotBPMNKeyW.PKENCRYPT.getValue())|| tagName.equals(DotBPMNKeyW.SKENCRYPT.getValue()))
				return new KEncrypt().interpreterTask(x, xmllines, datanodes);
			else if(tagName.equals(DotBPMNKeyW.PKCOMPUTATION.getValue())|| tagName.equals(DotBPMNKeyW.SKCOMPUTATION.getValue()))
				return new KComputation().interpreterTask(x, xmllines, datanodes);
			else if(tagName.equals(DotBPMNKeyW.PKDECRYPT.getValue())|| tagName.equals(DotBPMNKeyW.SKDECRYPT.getValue()))
				return new KDecrypt().interpreterTask(x, xmllines, datanodes);
			else if(tagName.equals(DotBPMNKeyW.MPC.getValue()))
				return new MPC().interpreterTask(x, xmllines, datanodes);
		}
		return null;
	}

	public static AbstractDataPET detectPETData(Elements xmllines) throws JSONException {
		for (Element x : xmllines) {
			String tagName = x.tagName();
			if (tagName.equals(DotBPMNKeyW.PKPUBLIC.getValue()))
				return new PKPublic().interpreterData(x, xmllines);
			else if(tagName.equals(DotBPMNKeyW.PKPRIVATE.getValue()))
				return new PKPrivate().interpreterData(x, xmllines);
		}
		return null;
	}
}
