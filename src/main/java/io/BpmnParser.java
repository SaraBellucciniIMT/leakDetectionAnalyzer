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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
//Change path extension
public class BpmnParser {

	public static Pair<Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>, Set<Pair<FlowNode, FlowNode>>> collaborationParser(
			String s) throws IOException {
		Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>> bpmnSet = new HashSet<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>();
		
		File file = new File(s);
		
		if(!file.exists())
			System.err.println(file.getName() + " doens't exist in this directory");
		Document doc = Jsoup.parse(new File(s), "UTF-8");

		Elements partecipant = doc.getElementsByTag("bpmn2:process");

		for (int i = 0; i < partecipant.size(); i++) {
			Bpmn<BpmnControlFlow<FlowNode>, FlowNode> bpmn = new Bpmn<BpmnControlFlow<FlowNode>, FlowNode>();

			Element el = partecipant.get(i);
			String idpartecipant = el.attr("id");
			String namepartecipant = el.attr("name");
			bpmn.setId(getCollaborationName(doc.getElementsByTag("bpmn2:collaboration"), idpartecipant));
			if (namepartecipant != null && !namepartecipant.isEmpty())
				bpmn.setName(getCollaborationName(doc.getElementsByTag("bpmn2:collaboration"), namepartecipant));
			else
				bpmn.setName(getCollaborationName(doc.getElementsByTag("bpmn2:collaboration"), idpartecipant));
			// Set of all data objects identified uniquely by their name
			Set<PETExtendedNode> datanodeSet = detectDataObject(el.getElementsByTag("bpmn2:dataobjectreference"));

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
		try {
			PET pet = null;
			pet = detectePet(childrens);
			if(pet!= null)
				f.setDescription(pet.getPET().toString());
			for (Element child : childrens) {
				if (child.tagName().equals("bpmn2:datainputassociation")) {
					String dataobjref = child.getElementsByTag("bpmn2:sourceref").text();
					datanodeset.stream().filter(p -> getIdDataNode(p).equals(dataobjref))
							.forEach(d -> d.addReadingFlowNode(f));
					if (pet != null && pet.getPET().equals(PETLabel.SSRECONTRUCTION)) {
						for (PETExtendedNode e : datanodeset) {
							if (getIdDataNode(e).equals(dataobjref))
								e.setPET(pet);
						}
					} else if (pet != null && pet.getPET().equals(PETLabel.SSCOMPUTATION)) {
						for (PETExtendedNode d : datanodeset) {
							//if (getIdDataNode(d).equals(dataobjref) && ((SScomputation) pet).containObjRef(dataobjref))
							if (((SScomputation) pet).containObjRef(getIdDataNode(d)))
								d.setPET(pet);
						}}
				} else if (child.tagName().equals("bpmn2:dataoutputassociation")) {
					String dataobjref = child.getElementsByTag("bpmn2:targetref").text();
					datanodeset.stream().filter(p -> getIdDataNode(p).equals(dataobjref))
							.forEach(d -> d.addWritingFlowNode(f));
					if (pet != null && pet.getPET().equals(PETLabel.SSSHARING)) {
						for (PETExtendedNode d : datanodeset) {
							if (getIdDataNode(d).equals(dataobjref))
								d.setPET(pet);
						}
					}
				} else
					continue;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static PET detectePet(Elements childrens) throws JSONException {
		for (Element child : childrens) {
			if (child.tagName().equals("pleak:sssharing")) {
				String ssssharing = child.getElementsByTag("pleak:sssharing").text();
				JSONObject obj = new JSONObject(ssssharing);
				int treshold = obj.getInt("treshold");
				int computation = obj.getInt("computationParties");

				return new SSsharing(treshold, computation);
			} else if (child.tagName().equals("pleak:sscomputation")) {
				String computation = child.getElementsByTag("pleak:sscomputation").text();
				JSONObject obj = new JSONObject(computation);
				int index = obj.getInt("groupId");
				JSONArray arr = obj.getJSONArray("inputs");
				List<String> listobjref = new ArrayList<String>();
				for (int i = 0; i < arr.length(); i++) {
					JSONArray ar;
					if ((ar = arr.getJSONObject(i).getJSONArray("inputs")) != null) {
						for (int j = 0; j < ar.length(); j++)
							listobjref.add(ar.getJSONObject(j).getString("id"));
					}

				}
				return new SScomputation(index, listobjref);
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
