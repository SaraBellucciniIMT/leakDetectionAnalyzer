package io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

import io.pet.PET;
import io.pet.PETLabel;
import io.pet.SScomputation;
import io.pet.SSreconstruction;
import io.pet.SSsharing;

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

			Element el = partecipant.get(i);
			String idpartecipant = el.attr("id");
			bpmn.setName(getCollaborationName(doc.getElementsByTag("bpmn2:collaboration"), idpartecipant));

			// Set of all data objects identified uniquely by their name
			Set<PETExtendedNode> datanodeSet = detectDataObject(el.getElementsByTag("bpmn2:dataobjectreference"));

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
		return null;
	}

	private static void detectAssociation(Elements childrens, Set<PETExtendedNode> datanodeset, FlowNode f) {
		PET pet = detectePet(childrens);
		for (Element child : childrens) {
			if (child.tagName().equals("bpmn2:datainputassociation")) {
				String dataobjref = child.getElementsByTag("bpmn2:sourceref").text();
				datanodeset.stream().filter(p -> getIdDataNode(p).equals(dataobjref))
						.forEach(d -> d.addReadingFlowNode(f));
				if (pet != null && pet.getPET().equals(PETLabel.SSRECONTRUCTION))
					datanodeset.stream().filter(p -> getIdDataNode(p).equals(dataobjref)).forEach(d -> d.setPET(pet));
				else if (pet != null && pet.getPET().equals(PETLabel.SSCOMPUTATION))
					datanodeset.stream().filter(p -> getIdDataNode(p).equals(dataobjref)).forEach(d -> {
						if (((SScomputation) pet).containObjRef(dataobjref))
							d.setPET(pet);
					});
				;
			} else if (child.tagName().equals("bpmn2:dataoutputassociation")) {
				String dataobjref = child.getElementsByTag("bpmn2:targetref").text();
				datanodeset.stream().filter(p -> getIdDataNode(p).equals(dataobjref))
						.forEach(d -> d.addWritingFlowNode(f));
				if (pet != null && pet.getPET().equals(PETLabel.SSSHARING))
					datanodeset.stream().filter(p -> getIdDataNode(p).equals(dataobjref)).forEach(d -> d.setPET(pet));
			} else
				continue;
		}

	}

	private static PET detectePet(Elements childrens) {
		for (Element child : childrens) {
			if (child.tagName().equals("pleak:sssharing")) {
				String ssssharing = child.getElementsByTag("pleak:sssharing").text();
				String[] attr = ssssharing.replace("{", "").replace("}", "").split(",");
				int treshold = -1;
				int computation = -1;
				for (int i = 0; i < attr.length; i++) {
					String[] subsplit = attr[i].split(":");
					if (subsplit[0].contains("treshold"))
						treshold = Integer.valueOf(subsplit[1]);
					else if (subsplit[0].contains("computationParties"))
						computation = Integer.valueOf(subsplit[1]);
				}
				return new SSsharing(treshold, computation);
			} else if (child.tagName().equals("pleak:sscomputation")) {
				String computation = child.getElementsByTag("pleak:sscomputation").text().replace("{", "").replace("}",
						"");
				int index = computation.indexOf("groupId");
				String tpm = computation.substring(index + 7 + 3, index + 7 + 4);
				String[] objref = computation.split("\"");
				List<String> listobjref = new ArrayList<String>();
				for (int i = 0; i < objref.length; i++) {
					if (objref[i].contains("DataObjectReference")) {
						listobjref.add(objref[i]);
					}
				}
				return new SScomputation(Integer.valueOf(tpm), listobjref);
			} else if (child.tagName().equalsIgnoreCase("pleak:SSReconstruction")) {
				return new SSreconstruction();
			}
		}
		return null;
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
				Element ee = itp.next();
				if (ee.attr("processref").equals(name)) {
					s = ee.attr("name");
					if (s.equals(""))
						s = ee.attr("id");
				}
			}
		}
		return s;
	}

	/*
	 * Create a Set of Data Object s.t : ID-Name
	 */
	private static Set<Pair<String, String>> dataobjrefMap = new HashSet<Pair<String, String>>();

	private static Set<PETExtendedNode> detectDataObject(Elements dataobref) {
		Set<PETExtendedNode> datanodeSet = new HashSet<PETExtendedNode>();
		dataobref.forEach(d -> {
			String name = d.attr("name");

			name = name.replace(".", "_");
			String[] differentnames = name.split(",");
			for (int i = 0; i < differentnames.length; i++) {
				PETExtendedNode dn = new PETExtendedNode();
				dn.setId(d.attr("id") + differentnames[i]);
				dn.setName(differentnames[i]);
				datanodeSet.add(dn);
				dataobjrefMap.add(Pair.of(d.attr("id"), name));
			}
		});
		return datanodeSet;
	}

	private static void setIdName(FlowNode f, Element e) {
		if (!e.attr("name").isEmpty()) {
			String name = e.attr("name");
			name = name.replace(" ", "_");
			f.setName(name);
		}
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
