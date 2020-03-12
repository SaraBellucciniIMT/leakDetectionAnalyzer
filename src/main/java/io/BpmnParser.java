package io;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jbpt.pm.DataNode;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.NonFlowNode;
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
import org.json.JSONStringer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.Sets;

import algo.AbstractTranslationAlg;
import algo.IDOperaion;
import io.pet.Cipher;
import io.pet.KComputation;
import io.pet.KDecrypt;
import io.pet.KEncrypt;
import io.pet.Key;
import io.pet.PET;
import io.pet.PETLabel;
import io.pet.SScomputation;
import io.pet.SSreconstruction;
import io.pet.SSsharing;
import rpstTest.Utils;

/*
 * Every partecipant is inteded as a bpmn model itself, not as a unique model
 */
//Change path extension
public class BpmnParser {
	private final static String collaboration = "bpmn2:collaboration";
	private final static String sssharing = "pleak:sssharing";
	private final static String sscomputation = "pleak:sscomputation";
	private final static String ssreconstruction = "pleak:ssreconstruction";
	private final static String addsssharing = "pleak:addsssharing";
	private final static String addsscomputation = "pleak:addsscomputation";
	private final static String addssreconstruction = "pleak:addssreconstruction";
	private final static String funsssharing = "pleak:funsssharing";
	private final static String funsscomputation = "pleak:funsscomputation";
	private final static String funssrecostruction = "pleak:funssreconstruction";
	private final static String pkencrypt = "pleak:pkencrypt";
	private final static String pkcomputation = "pleak:pkcomputation";
	private final static String pkdecrypt = "pleak:pkdecrypt";
	private final static String skencrypt = "pleak:skencrypt";
	private final static String skcomputation = "pleak:skcomputation";
	private final static String skdecrypt = "pleak:skdecrypt";
	private final static String datainput = "bpmn2:datainputassociation";
	private final static String dataoutput = "bpmn2:dataoutputassociation";
	private final static String attrid = "id";
	private final static String attrname = "name";
	// public static SSreconstruction uniquereconstruction = null;
	// private static int id =0;
	private static Set<SSsharing> setssharing = new HashSet<SSsharing>();
	private static Set<SScomputation> setsscomputation = new HashSet<SScomputation>();

	public static Pair<Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>, Set<Pair<FlowNode, FlowNode>>> collaborationParser(
			String s) throws IOException {
		Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>> bpmnSet = new HashSet<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>();

		File file = new File(s);
		while (!file.exists()) {
			System.err.println(file.getName() + " doens't exist in this directory, try again");
			break;
		}
		Document doc = Jsoup.parse(new File(s), "UTF-8");
		Elements partecipant = doc.getElementsByTag("bpmn2:process");

		for (int i = 0; i < partecipant.size(); i++) {
			Bpmn<BpmnControlFlow<FlowNode>, FlowNode> bpmn = new Bpmn<BpmnControlFlow<FlowNode>, FlowNode>();
			Element el = partecipant.get(i);
			String idpartecipant = el.attr(attrid);
			String namepartecipant = el.attr(attrname);
			bpmn.setId(getCollaborationName(doc.getElementsByTag(collaboration), idpartecipant));
			if (namepartecipant != null && !namepartecipant.isEmpty())
				bpmn.setName(getCollaborationName(doc.getElementsByTag(collaboration), namepartecipant));
			else
				bpmn.setName(getCollaborationName(doc.getElementsByTag(collaboration), idpartecipant));
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

		// setTresholdRecostruction();
		// Compute message flow
		Elements collaborationel = doc.getElementsByTag(collaboration);

		Set<Pair<FlowNode, FlowNode>> messageflow = new HashSet<Pair<FlowNode, FlowNode>>();
		collaborationel.forEach(c -> {
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
		correctData(bpmnSet);
		return Pair.of(bpmnSet, messageflow);
	}

	private static void correctData(Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>> setbpmns) {
		for (Bpmn<BpmnControlFlow<FlowNode>, FlowNode> b : setbpmns) {
			for (NonFlowNode n : b.getNonFlowNodes()) {
				PETExtendedNode pet = (PETExtendedNode) n;
				for (Entry<String, PET> entry : dataobjrefMap.entrySet()) {
					if (entry.getKey().equals(pet.getName())) {
						pet.setPET(entry.getValue());
					}
				}
			}
		}
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
			if ((AbstractTranslationAlg.id_op == IDOperaion.SSSHARING.getVal()) ||(AbstractTranslationAlg.id_op == IDOperaion.ENCRYPTION.getVal()) || AbstractTranslationAlg.id_op == IDOperaion.RECONSTRUCTION.getVal())
				pet = detectePet(childrens, datanodeset);
			int coundsharesadditivesss = 0;
			if (pet != null) {
				if (pet.getPET().equals(PETLabel.SSSHARING) || pet.getPET().equals(PETLabel.KENCRYPT))
					f.setDescription(pet.getPET().name() + "-" + pet.getIdPet());
				else
					f.setDescription(pet.getPET().name());
			}
			for (Element child : childrens) {
				if (child.tagName().equals(datainput)) {
					String dataobjref = child.getElementsByTag("bpmn2:sourceref").text();
					datanodeset.stream().filter(p -> getIdDataNode(p).equals(dataobjref))
							.forEach(d -> d.addReadingFlowNode(f));
				} else if (child.tagName().equals(dataoutput)) {
					String dataobjref = child.getElementsByTag("bpmn2:targetref").text();
					datanodeset.stream().filter(p -> getIdDataNode(p).equals(dataobjref))
							.forEach(d -> d.addWritingFlowNode(f));
					if (pet != null) {
						if (pet.getPET().equals(PETLabel.SSSHARING)) {
							for (PETExtendedNode d : datanodeset) {
								if (getIdDataNode(d).equals(dataobjref)) {
									d.setPET(pet);
									addtoDaraObjMap(d.getName(), pet);
									if (((SSsharing) pet).getThreshold() == -1)
										coundsharesadditivesss += 1;
								}
							}
							// If the thresold is not already set this means that is an addictive ssharing
							// so the threashold is equal to the number of output data

						} else if (pet.getPET().equals(PETLabel.SSCOMPUTATION)) {
							for (PETExtendedNode e : datanodeset) {
								if (getIdDataNode(e).equals(dataobjref) && !e.hasPET()) {
									e.setPET(pet);
									addtoDaraObjMap(e.getName(), pet);
								}
							}
						} else if (pet.getPET().equals(PETLabel.SSRECONTRUCTION)) {
							for (PETExtendedNode e : datanodeset) {
								if (getIdDataNode(e).equals(dataobjref)) {
									e.setPET(pet);
									addtoDaraObjMap(e.getName(), pet);
								}
							}
						} else if (pet.getPET().equals(PETLabel.KENCRYPT)) {
							// This is the cipher object going out from the encryption task
							for (PETExtendedNode e : datanodeset) {
								if (getIdDataNode(e).equals(dataobjref)) {
									Cipher c = new Cipher();
									c.setIdPet(((KEncrypt) pet).getKey().getIdPet());
									e.setPET(c);
									addtoDaraObjMap(e.getName(), c);
								}
							}
						} else if (pet.getPET().equals(PETLabel.KCOMPUTATION)) {
							for (PETExtendedNode e : datanodeset) {
								if (getIdDataNode(e).equals(dataobjref)) {
									Cipher c = new Cipher();
									c.setIdPet(((KComputation) pet).getIdCipher());
									e.setPET(c);
									addtoDaraObjMap(e.getName(), c);
								}
							}

						}
					}
				}
				continue;
			}
			if (coundsharesadditivesss != 0) {
				((SSsharing) pet).setThreshold(coundsharesadditivesss);
				coundsharesadditivesss = 0;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private static PET detectePet(Elements childrens, Set<PETExtendedNode> datanodeset) throws JSONException {
		for (Element child : childrens) {
			String tagname = child.tagName();
			if (tagname.equalsIgnoreCase(sssharing) || tagname.equalsIgnoreCase(addsssharing)
					|| tagname.equalsIgnoreCase(funsssharing)) {
				SSsharing ssharing = new SSsharing(Utils.getId());
				if (tagname.equals(sssharing)) {
					String ssssharing = child.getElementsByTag(tagname).text();
					JSONObject obj = new JSONObject(ssssharing);
					int treshold = obj.getInt("treshold");
					ssharing.setThreshold(treshold);
				} else if (tagname.equals(funsssharing)) {
					ssharing.setThreshold(2);
				}
				setssharing.add(ssharing);
				return ssharing;
			} else if (tagname.equalsIgnoreCase(sscomputation) || tagname.equalsIgnoreCase(addsscomputation)
					|| tagname.equalsIgnoreCase(funsscomputation)) {
				String computation = child.getElementsByTag(tagname).text();
				JSONObject obj = new JSONObject(computation);
				String objgroup = obj.getString("groupId");
				for (SScomputation s : setsscomputation) {
					if (s.getGroupId().equals(objgroup))
						return s;
				}
				SScomputation sscomputatin = new SScomputation(objgroup);
				setsscomputation.add(sscomputatin);
				return sscomputatin;

			} else if (tagname.equalsIgnoreCase(ssreconstruction) || tagname.equalsIgnoreCase(addssreconstruction)
					|| tagname.equalsIgnoreCase(funssrecostruction)) {
				return new SSreconstruction(Utils.getId());
			} else if (tagname.equals(pkencrypt)) {
				JSONObject obj = new JSONObject(child.getElementsByTag(tagname).text());
				KEncrypt kencrypt = new KEncrypt();
				String key = obj.getString("key");
				datanodeset.forEach(d -> {
					if (getIdDataNode(d).equals(key)) {
						Key k = new Key();
						mapkeygroupid.forEach((g, p) -> {
							if (p.getRight().contains(d.getName()))
								k.setIdPet(p.getLeft());
						});
						kencrypt.setKey(k);
						// addtoDaraObjMap(d.getName(), k);
					}
				});
				return kencrypt;
			} else if (tagname.equals(skencrypt)) {
				JSONObject obj = new JSONObject(child.getElementsByTag(tagname).text());
				KEncrypt kencrypt = new KEncrypt();
				String key = obj.getString("key");
				datanodeset.forEach(d -> {
					if (getIdDataNode(d).equals(key)) {
						Key k = new Key();
						k.setIdPet(Utils.getId());
						kencrypt.setKey(k);
						addtoDaraObjMap(d.getName(), k);
					}
				});
				return kencrypt;
			} else if (tagname.equals(pkcomputation) || tagname.equals(skcomputation)) {
				KComputation kcomputation = new KComputation(Utils.getId());
				JSONObject obj = new JSONObject(child.getElementsByTag(tagname).text());
				JSONArray arr = obj.getJSONArray("inputTypes");
				for (int i = 0; i < arr.length(); i++) {
					JSONObject objtype = arr.getJSONObject(i);
					String s = objtype.getString("type");
					if (s.equals("encrypted")) {
						String data = objtype.getString("id");
						datanodeset.forEach(d -> {
							if (getIdDataNode(d).equals(data)) {
								Cipher c = new Cipher();
								mapkeygroupid.forEach((g, p) -> {
									if (p.getRight().contains(d.getName()))
										c.setIdPet(p.getLeft());
								});
								kcomputation.setCipher(c);
							}
						});
					}
				}
				return kcomputation;
			} else if (tagname.equals(pkdecrypt) || tagname.equals(skdecrypt)) {
				KDecrypt kdecrypt = new KDecrypt(Utils.getId());
				// String key = obj.getString("key");
				/*
				 * datanodeset.forEach(d -> { if (getIdDataNode(d).equals(key))
				 * kdecrypt.setKeyDecypt(d.getName()); });
				 */
				// String ciphertext = obj.getString("ciphertext");
				/*
				 * datanodeset.forEach(d -> { if (getIdDataNode(d).equals(ciphertext))
				 * kdecrypt.setCipherText(d.getName()); }); kdecrypt.setKeyDecypt(key);
				 * kdecrypt.setCipherText(ciphertext);
				 */
				return kdecrypt;
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
					s = ee.attr(attrname);
					if (s.equals(""))
						s = ee.attr(attrid);
				}
			}
		}
		return s;
	}

	/*
	 * Create a Set of Data Object s.t : ID-Name
	 */
	private static Map<String, PET> dataobjrefMap = new HashMap<String, PET>();

	private static void addtoDaraObjMap(String s, PET p) {
		boolean change = false;
		for (Entry<String, PET> entry : dataobjrefMap.entrySet()) {
			if (entry.getKey().equals(s)) {
				change = true;
			}
		}
		if (!change)
			dataobjrefMap.put(s, p);
	}

	private static Map<String, Pair<Integer, Set<String>>> mapkeygroupid = new HashMap<String, Pair<Integer, Set<String>>>();

	private static Set<PETExtendedNode> detectDataObject(Elements datapobref) {
		Set<PETExtendedNode> datanodeSet = new HashSet<PETExtendedNode>();
		for (Element d : datapobref) {
			String name = d.attr(attrname);
			name = name.replace(".", "_");
			String[] differentnames = name.split(",");
			for (int i = 0; i < differentnames.length; i++) {
				PETExtendedNode dn = new PETExtendedNode();
				dn.setId(d.attr("id") + differentnames[i]);
				dn.setName(differentnames[i]);
				datanodeSet.add(dn);

				for (Entry<String, PET> entry : dataobjrefMap.entrySet()) {
					if (entry.getKey().equals(differentnames[i]))
						dn.setPET(entry.getValue());
				}
				for (Element child : d.children()) {
					// In case of private key encryption
					if (child.tagName().equals("pleak:pkprivate") || child.tagName().equals("pleak:pkpublic")) {
						String pkp = child.getElementsByTag(child.tagName()).text();
						JSONObject obj;
						try {
							obj = new JSONObject(pkp);
							String objgroup = obj.getString("groupId");
							Key k = new Key();
							if (mapkeygroupid.containsKey(objgroup)) {
								k.setIdPet(mapkeygroupid.get(objgroup).getLeft());
								mapkeygroupid.get(objgroup).getRight().add(dn.getName());
							} else {
								int id = Utils.getId();
								k.setIdPet(id);
								mapkeygroupid.put(objgroup, Pair.of(id, Sets.newHashSet(dn.getName())));
							}

							if (child.tagName().equals("pleak:pkprivate")) {
								dn.setPET(k);
								addtoDaraObjMap(dn.getName(), k);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}
			}
		}
		return datanodeSet;
	}

	private static void setIdName(FlowNode f, Element e) {
		if (!e.attr(attrname).isEmpty()) {
			String name = e.attr(attrname);
			name = name.replace(" ", "_");
			f.setName(name);
		}
		f.setId(e.attr(attrid));
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
