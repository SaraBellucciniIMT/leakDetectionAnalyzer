package io;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jbpt.pm.DataNode;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.bpmn.AlternativeGateway;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.bpmn.BpmnControlFlow;
import org.jbpt.pm.bpmn.CatchingEvent;
import org.jbpt.pm.bpmn.EndEvent;
import org.jbpt.pm.bpmn.StartEvent;
import org.jbpt.pm.bpmn.Task;

import org.json.JSONException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.pet.AbstractDataPET;
import io.pet.AbstractTaskPET;
import io.pet.PETInterpreter;

/**
 * 
 * The BpmnParser class provides utilities to parse single and collaboration
 * bpmn models. In a collaboration every participant is intended as a bpmn model
 * itself where ID = id NAME = processRef Description = name (is a description
 * since sometimes can be empty)
 * 
 * @author S. Belluccini
 *
 */
public class BpmnParser {

	/**
	 * Parse NON-collaboration BPMN models
	 * 
	 * @param f the file .bpmn to parse
	 * @return the object of the BPMN model parsed
	 * @throws IOException if the given file cannot be parsed
	 */
	public static Bpmn<BpmnControlFlow<FlowNode>, FlowNode> singleParser(File f) throws IOException {
		Bpmn<BpmnControlFlow<FlowNode>, FlowNode> bpmn = new Bpmn<BpmnControlFlow<FlowNode>, FlowNode>();
		Document doc = Jsoup.parse(f, "UTF-8");
		String nameprocess = doc.getElementsByTag(DotBPMNKeyW.PROCESS.getValue()).attr(DotBPMNKeyW.ID.getValue());
		bpmn.setName(nameprocess);
		bpmn = constructBPMN(doc.getElementById(nameprocess));
		return bpmn;
	}

	/**
	 * Returns the set of BPMN elements in this collaboration with the set of
	 * messages exchanged between their flownodes
	 * 
	 * @param f the file .bpmn to be parse
	 * @return a pair with the sets of bpmn objects and a set of pairs representing
	 *         the message exchange between flownodes
	 */
	public static Pair<Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>, Set<Pair<FlowNode, FlowNode>>> collaborationParser(
			File f) throws IOException {
		Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>> bpmnSet = new HashSet<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>();
		Document doc = Jsoup.parse(f, "UTF-8");
		Elements partecipant = doc.getElementsByTag(DotBPMNKeyW.PROCESS.getValue());
		for (int i = 0; i < partecipant.size(); i++) {
			Element p = partecipant.get(i);
			Bpmn<BpmnControlFlow<FlowNode>, FlowNode> bpmn = constructBPMN(p);
			// Set Id and Name of the participant
			String id = p.getElementsByTag(DotBPMNKeyW.PROCESS.getValue()).attr(DotBPMNKeyW.ID.getValue());
			bpmn.setId(id);
			bpmn.setName(retriveCollaborationName(doc.getElementsByTag(DotBPMNKeyW.PARTICIPANT.getValue()), id));
			bpmnSet.add(bpmn);
		}
		// Detect message flow
		Elements collaborationel = doc.getElementsByTag(DotBPMNKeyW.COLLABORATION.getValue());
		Set<Pair<FlowNode, FlowNode>> messageflow = new HashSet<Pair<FlowNode, FlowNode>>();
		collaborationel.forEach(c -> {
			for (Element e : c.children()) {
				if (e.tagName().equals(DotBPMNKeyW.MESSAGEFLOW.getValue())) {
					FlowNode entry = detectSenderReceiver(bpmnSet, e.attr(DotBPMNKeyW.SOURCEREF.getValue()));
					FlowNode exit = detectSenderReceiver(bpmnSet, e.attr(DotBPMNKeyW.TARGETREF.getValue()));
					messageflow.add(Pair.of(entry, exit));
				}
			}
		});
		
		return Pair.of(bpmnSet, messageflow);
	}

	/**
	 * Construct the BPMN object of a single participant
	 * 
	 * @param elements the part of XML code to be analyzed
	 * @return an object representing a BPMN
	 */
	private static Bpmn<BpmnControlFlow<FlowNode>, FlowNode> constructBPMN(Element elements) {
		Bpmn<BpmnControlFlow<FlowNode>, FlowNode> bpmn = new Bpmn<BpmnControlFlow<FlowNode>, FlowNode>();
		Set<DataNode> datanodeSet = detectDataObject(elements.getElementsByTag(DotBPMNKeyW.DATAOBJECTREF.getValue()));
		for (Element e : elements.children()) {
			FlowNode f = null;
			if (e.tagName().equals(DotBPMNKeyW.STARTEVENT.getValue())) {
				f = new StartEvent();
				f.setDescription(BPMNLabel.STARTEVENT.name());
			} else if (e.tagName().equals(DotBPMNKeyW.ENDEVENT.getValue())) {
				f = new EndEvent();
				f.setDescription(BPMNLabel.ENDEVENT.name());
			} else if (e.tagName().equals(DotBPMNKeyW.TASK.getValue())) {
				f = new Task();
				f.setDescription(BPMNLabel.TASK.name());
				detectAssociation(e.children(), datanodeSet, f);
				AbstractTaskPET pet = null;
				try {
					pet = PETInterpreter.detectPETTask(e.children(),datanodeSet);
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
				f.setTag(pet);
			} else if (e.tagName().equals(DotBPMNKeyW.INTERCATCHEVENT.getValue())) {
				f = new CatchingEvent();
				f.setDescription(BPMNLabel.INTERCATCHMESSAGE.name());
				detectAssociation(e.children(), datanodeSet, f);
			} else if (e.tagName().equals(DotBPMNKeyW.EXCLUSIVEGAT.getValue())) {
				f = new AlternativeGateway();
				f.setDescription(BPMNLabel.XOR.name());
			} else if (e.tagName().equals(DotBPMNKeyW.PARALLELGAT.getValue())) {
				f = new AlternativeGateway();
				f.setDescription(BPMNLabel.AND.name());
			} else if (e.tagName().equals(DotBPMNKeyW.EVENTBASEDGAT.getValue())) {
				f = new AlternativeGateway();
				f.setDescription(BPMNLabel.EVENTBASEDG.name());
			} else {
				continue;
			}
			setIdName(f, e);
			bpmn.addFlowNode(f);
		}
		datanodeSet.forEach(nfn -> bpmn.addNonFlowNode(nfn));
		defineControlFlow(bpmn, elements.getElementsByTag(DotBPMNKeyW.SEQUENCEFLOW.getValue()));
		return bpmn;
	}

	/**
	 * Returns the FlowNode corresponding to the given id
	 * 
	 * @param bpmnSet the collection of bpmn on which we search
	 * @param id      the string that we want to identify
	 * @return the FlowNode corresponding to the given id
	 */
	private static FlowNode detectSenderReceiver(Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>> bpmnSet, String id) {
		for (Bpmn<BpmnControlFlow<FlowNode>, FlowNode> f : bpmnSet) {
			FlowNode node;
			if ((node = getFlowNode(f.getFlowNodes(), id)) != null)
				return node;
		}
		return null;
	}

	/**
	 * Returns the flownode with a specific id in the collection
	 * 
	 * @param nodes collection of node to analyze
	 * @param id    the string to identify the node
	 * @return the flownode with a specific id in the collection
	 */
	private static FlowNode getFlowNode(Collection<FlowNode> nodes, String id) {
		Iterator<FlowNode> it = nodes.iterator();
		while (it.hasNext()) {
			FlowNode fn = it.next();
			if (fn.getId().equals(id))
				return fn;
		}
		return null;
	}

	/**
	 * Detect the set of reading and writing data objects of a given element f
	 * 
	 * @param childrens   the line of codes of the element that we are analyzing
	 * @param datanodeset the set of data node existing in this model
	 * @param f           the flow
	 */
	private static void detectAssociation(Elements childrens, Set<DataNode> datanodeset, FlowNode f) {
		for (Element child : childrens) {
			if (child.tagName().equals(DotBPMNKeyW.DATAINPUTASS.getValue())) {
				String dataobjref = child.getElementsByTag(DotBPMNKeyW.BPMNSOURCEREF.getValue()).text();
				datanodeset.stream().filter(p -> p.getId().equals(dataobjref)).forEach(d -> d.addReadingFlowNode(f));
			} else if (child.tagName().equals(DotBPMNKeyW.DATAOUTPUTASS.getValue())) {
				String dataobjref = child.getElementsByTag(DotBPMNKeyW.BPMNTARGETREF.getValue()).text();
				datanodeset.stream().filter(p -> p.getId().equals(dataobjref)).forEach(d -> d.addWritingFlowNode(f));
			}
		}
	}

	/**
	 * Returns the processRef, i.e. the name that the participant has in the model
	 * 
	 * @param elements lines of code to analyze
	 * @param name     id of the participant of which we want to know the name
	 * @return the processRef, i.e. the name that the participant has in the model
	 */
	private static String retriveCollaborationName(Elements elements, String id) {
		for (Element e : elements) {
			if (e.attr("processref").equals(id)) {
				return e.attr(DotBPMNKeyW.NAME.getValue());
			}
		}
		return null;
	}
	
	/**
	 * Detect data objects in this piece of code
	 * 
	 * @param datapobref piece of code to analyze
	 * @return set of data objects that have has id and name the corrispondent
	 *         attributes in the bpmn file
	 */
	private static Set<DataNode> detectDataObject(Elements datapobref) {
		Set<DataNode> datanodeSet = new HashSet<DataNode>();
		for (Element d : datapobref) {
			AbstractDataPET pet = null;
			if (!d.children().isEmpty()) {
				try {
					pet = PETInterpreter.detectPETData(d.children());
				} catch (JSONException e) {
					e = new JSONException("Reading went wrong");
				}
			}
			String name = d.attr(DotBPMNKeyW.NAME.getValue());
			String id = d.attr(DotBPMNKeyW.ID.getValue());
			DataNode node = new DataNode(name);
			node.setId(id);
			if (pet != null)
				node.setTag(pet);
			datanodeSet.add(node);
		}
		return datanodeSet;
	}

	/**
	 * Sets the id and name of a flownode
	 * 
	 * @param f the flownode
	 * @param e the element of the bpmn file
	 */
	private static void setIdName(FlowNode f, Element e) {
		f.setName(e.attr(DotBPMNKeyW.NAME.getValue()));
		f.setId(e.attr(DotBPMNKeyW.ID.getValue()));
	}

	/**
	 * Adds the connection among elements in the bpmn model
	 * 
	 * @param bpmn     the model to which we wanto to add the connections
	 * @param elements the line of code analyze to search for the connections
	 */
	private static void defineControlFlow(Bpmn<BpmnControlFlow<FlowNode>, FlowNode> bpmn, Elements elements) {
		for (Element e : elements) {
			bpmn.addControlFlow(getFlowNode(bpmn.getFlowNodes(), e.attr(DotBPMNKeyW.SOURCEREF.getValue())),
					getFlowNode(bpmn.getFlowNodes(), e.attr(DotBPMNKeyW.TARGETREF.getValue())));
		}
	}

}
