package io;

import java.util.Set;

import org.jbpt.pm.FlowNode;
import org.jbpt.pm.bpmn.AlternativeGateway;
import org.jbpt.pm.bpmn.CatchingEvent;
import org.jbpt.pm.bpmn.EndEvent;
import org.jbpt.pm.bpmn.StartEvent;
import org.jbpt.pm.bpmn.Task;
import org.jsoup.nodes.Element;

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
	
	ID("id"),
	NAME("name"),
	PROCESSREF("processref"),
	SOURCEREF("sourceref"),
	TARGETREF("targetref");
	
	private String value;
	
	private DotBPMNKeyW(String value) {
		this.value = value;
	}

	public String toString() {
		return this.value;
	}
}
