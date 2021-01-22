package io;

/**
 * Enum class with string tag from the XML .bpmn file
 * @author Sara
 *
 */
public enum DotBPMNKeyW {
	
	COLLABORATION("bpmn2:collaboration"),
	PROCESS("bpmn2:process"),
	MESSAGEFLOW("bpmn2:messageflow"),
	SEQUENCEFLOW("bpmn2:sequenceflow"),
	DATAINPUTASS("bpmn2:datainputassociation"),
	DATAOUTPUTASS("bpmn2:dataoutputassociation"),
	PARTICIPANT("bpmn2:participant"),
	DATAOBJECTREF("bpmn2:dataobjectreference"),
	STARTEVENT("bpmn2:startevent"),
	ENDEVENT("bpmn2:endevent"),
	TASK("bpmn2:task"),
	INTERCATCHEVENT("bpmn2:intermediatecatchevent"),
	EXCLUSIVEGAT("bpmn2:exclusivegateway"),
	PARALLELGAT("bpmn2:parallelgateway"),
	EVENTBASEDGAT("bpmn2:eventbasedgateway"),
	
	SSSHARING("pleak:sssharing"),
	SSCOMPUTATION("pleak:sscomputation"),
	SSRECONSTRUCTION("pleak:ssreconstruction"),
	ADDSSSHARING("pleak:addsssharing"),
	ADDSSCOMPUTATION("pleak:addsscomputation"),
	ADDSSRECONSTRUCTION("pleak:addssreconstruction"),
	FUNSSSHARING("pleak:funsssharing"),
	FUNSSCOMPUTATION("pleak:funsscomputation"),
	FUNSSRECONSTRUCTION("pleak:funssreconstruction"),
	PKENCRYPT("pleak:pkencrypt"),
	PKCOMPUTATION("pleak:pkcomputation"),
	PKDECRYPT("pleak:pkdecrypt"),
	SKENCRYPT("pleak:skencrypt"),
	SKCOMPUTATION("pleak:skcomputation"),
	SKDECRYPT("pleak:skdecrypt"),
	PKPUBLIC("pleak:pkpublic"),
	PKPRIVATE("pleak:pkprivate"),
	MPC("pleak:mpc"),
	
	ID("id"),
	NAME("name"),
	PUBLIC("public"),
	ENCRYPTED("encrypted"),
	PROCESSREF("processref"),
	BPMNSOURCEREF("bpmn2:sourceref"),
	BPMNTARGETREF("bpmn2:targetref"),
	SOURCEREF("sourceref"),
	TARGETREF("targetref"),
	TRESHOLD("treshold"),
	GROUPID("groupId"),
	KEY("key"),
	TYPE("type"),
	INPUTS("inputs"),
	INPUTTYPES("inputTypes");
	
	private String value;
	
	private DotBPMNKeyW(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}
	
	@Override
	public String toString() {
		return this.value;
	}
}
