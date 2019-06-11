package io;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/*
 * Every partecipant is inteded as a bpmn model itself, not as a unique model
 */
public class BpmnParser {

	public static Pair<Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>, Set<Pair<FlowNode, FlowNode>>> collaborationParser(
			String s) throws IOException {
		Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>> bpmnSet = new HashSet<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>();
		Document doc = Jsoup.parse(new File(s), "UTF-8");

		Elements partecipant = doc.getElementsByTag("bpmn2:process");

		for (int i = 0; i < partecipant.size(); i++) {
			Bpmn<BpmnControlFlow<FlowNode>, FlowNode> bpmn = new Bpmn<BpmnControlFlow<FlowNode>, FlowNode>();

			String idpartecipant = partecipant.attr("id");
			bpmn.setName(getCollaborationName(doc.getElementsByTag("bpmn2:collaboration"), idpartecipant));
			Element el = partecipant.get(i);
			// Set of all data objects identified uniquely by their name
			Set<DataNode> datanodeSet = detectDataObject(el.getElementsByTag("bpmn2:dataobjectreference"));

			// Set<FlowNode> allNodes = new HashSet<FlowNode>();
			for (Element e : el.children()) {
				FlowNode f = null;

				if (e.tagName().equals("bpmn2:startevent")) {
					f = new StartEvent();
					f.setTag(BPMNLabel.STARTEVENT);
				} else if (e.tagName().equals("bpmn2:endevent")) {
					f = new EndEvent();
					f.setTag(BPMNLabel.ENDEVENT);
				} else if (e.tagName().equals("bpmn2:task")) {
					f = new Task();
					f.setTag(BPMNLabel.TASK);
					detectAssociation(e.children(), datanodeSet, f);
				} else if (e.tagName().equals("bpmn2:intermediatecatchevent")) {
					f = new CatchingEvent();
					f.setTag(BPMNLabel.MESSAGE);
					detectAssociation(e.children(), datanodeSet, f);
				} else if (e.tagName().equals("bpmn2:exclusivegateway")) {
					f = new AlternativeGateway();
					f.setTag(BPMNLabel.XOR);
				} else if (e.tagName().equals("bpmn2:parallelgateway")) {
					f = new AlternativeGateway();
					f.setTag(BPMNLabel.AND);
				} else {
					continue;
				}
				setIdName(f, e);
				bpmn.addFlowNode(f);
			}
			datanodeSet.forEach(nfn -> bpmn.addNonFlowNode(nfn));

			defineControlFlow(bpmn, el.getElementsByTag("bpmn2:sequenceflow"));
			bpmnSet.add(bpmn);
		}
		// Compute message flow
		Elements collaboration = doc.getElementsByTag("bpmn2:collaboration");

		Set<Pair<FlowNode, FlowNode>> messageflow = new HashSet<Pair<FlowNode, FlowNode>>();
		collaboration.forEach(c -> {
			for (Element e : c.children()) {
				if (e.tagName().equals("bpmn2:messageflow")) {
					Pair<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>, FlowNode> entry = detectSenderReceiver(bpmnSet,
							e.attr("sourceref"));
					Pair<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>, FlowNode> exit = detectSenderReceiver(bpmnSet,
							e.attr("targetref"));
					messageflow.add(Pair.of(entry.getRight(), exit.getRight()));
				}
			}
		});

		return Pair.of(bpmnSet, messageflow);
	}

	private static Pair<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>, FlowNode> detectSenderReceiver(
			Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>> bpmnSet, String id) {
		for (Bpmn<BpmnControlFlow<FlowNode>, FlowNode> f : bpmnSet) {
			FlowNode entry;
			if ((entry = getFlowNode(f.getFlowNodes(), id)) != null)
				return Pair.of(f, entry);
		}
		System.err.println("There is no receive or send for the message");
		return null;
	}

	private static void detectAssociation(Elements childrens, Set<DataNode> datanodeset, FlowNode f) {
		for (Element child : childrens) {
			if (child.tagName().equals("bpmn2:datainputassociation")) {
				String dataobjref = child.getElementsByTag("bpmn2:sourceref").text();
			
				datanodeset.stream().filter(p -> getIdDataNode(p).equals(dataobjref))
						.forEach(d -> d.addReadingFlowNode(f));
				;
			} else if (child.tagName().equals("bpmn2:dataoutputassociation")) {
				String dataobjref = child.getElementsByTag("bpmn2:targetref").text();
				datanodeset.stream().filter(p -> getIdDataNode(p).equals(dataobjref))
						.forEach(d -> d.addWritingFlowNode(f));
			} else
				continue;
		}
	}

	/*
	 * DataNode composition: ID _ NAME
	 */
	private static String getIdDataNode(DataNode d) {
		int spliti = d.getId().lastIndexOf(d.getName());
		return d.getId().substring(0, spliti);
	}

	private static String getCollaborationName(Elements elements, String name) {
		String s = "";
		Iterator<Element> it = elements.iterator();
		while (it.hasNext()) {
			Element e = it.next();
			Elements partecipants = e.getElementsByTag("bpmn2:participant");
			Iterator<Element> itp = partecipants.iterator();
			while (itp.hasNext()) {
				if (itp.next().attr("processref").equals(name)) {
					s = itp.next().attr("name");
					if (s.equals(""))
						s = itp.next().attr("id");
				}
			}
		}
		return s;
	}

	/*
	 * Create a Set of Data Object identified by their Name
	 */
	private static Set<Pair<String, String>> dataobjrefMap = new HashSet<Pair<String, String>>();

	private static Set<DataNode> detectDataObject(Elements dataobref) {
		Set<DataNode> datanodeSet = new HashSet<DataNode>();
		dataobref.forEach(d -> {
			String name = d.attr("name");
			String[] differentnames = name.split(",");
			for (int i = 0; i < differentnames.length; i++) {
				DataNode dn = new DataNode();
				dn.setId(d.attr("id") + differentnames[i]);
				dn.setName(differentnames[i]);
				datanodeSet.add(dn);
				dataobjrefMap.add(Pair.of(d.attr("id"), name));
			}
		});
		return datanodeSet;
	}

	private static void setIdName(FlowNode f, Element e) {
		if (!e.attr("name").isEmpty())
			f.setName(e.attr("name"));
		f.setId(e.attr("id"));
	}

	/*
	 * Return the flow node with ID == to the ID in input
	 */
	private static FlowNode getFlowNode(Collection<FlowNode> collection, String id) {
		Iterator<FlowNode> it = collection.iterator();
		while (it.hasNext()) {
			FlowNode fn = it.next();
			if (fn.getId().equals(id))
				return fn;
		}
		return null;
	}

	private static void defineControlFlow(Bpmn<BpmnControlFlow<FlowNode>, FlowNode> bpmn, Elements elements) {
		for (Element e : elements) {
			bpmn.addControlFlow(getFlowNode(bpmn.getFlowNodes(), e.attr("sourceref")),
					getFlowNode(bpmn.getFlowNodes(), e.attr("targetref")));
		}
	}

}
