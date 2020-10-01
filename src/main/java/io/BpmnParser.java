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
 * Every partecipant is inteded as a bpmn model itself where
 * ID = id
 * NAME = processRef
 * Description = name (is a description since sometimes can be empty)
 */
//Change path extension
public class BpmnParser {
	
	// public static SSreconstruction uniquereconstruction = null;
	// private static int id =0;
	//private static Set<SSsharing> setssharing = new HashSet<SSsharing>();
	//private static Set<SScomputation> setsscomputation = new HashSet<SScomputation>();

	// Parse single bpmn model ".bpmn"
	public static Bpmn<BpmnControlFlow<FlowNode>, FlowNode> singleParser(File f) throws IOException {
		Bpmn<BpmnControlFlow<FlowNode>, FlowNode> bpmn = new Bpmn<BpmnControlFlow<FlowNode>, FlowNode>();

		Document doc = Jsoup.parse(f, "UTF-8");
		
		String nameprocess = doc.getElementsByTag(DotBPMNKeyW.PROCESS.toString()).attr(DotBPMNKeyW.ID.toString());
		bpmn.setName(nameprocess);
		
		bpmn = constructBPMN(doc.getElementById(nameprocess));
		
		return bpmn;
	}
	
	public static Pair<Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>, Set<Pair<FlowNode, FlowNode>>> collaborationParser(
			File f) throws IOException {
		Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>> bpmnSet = new HashSet<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>();

		Document doc = Jsoup.parse(f, "UTF-8");
		Elements partecipant = doc.getElementsByTag(DotBPMNKeyW.PROCESS.toString());

		for (int i = 0; i < partecipant.size(); i++) {
			Element p = partecipant.get(i);
			
			Bpmn<BpmnControlFlow<FlowNode>, FlowNode> bpmn = constructBPMN(p);
			
			//Set ID-NAME-DESCRIPTION of the current party
			String nameparty = p.getElementsByTag(DotBPMNKeyW.PROCESS.toString()).attr(DotBPMNKeyW.ID.toString());
			bpmn.setName(nameparty);
			Pair<String,String> id_description = retriveCollaborationName(doc.getElementsByTag(DotBPMNKeyW.PARTICIPANT.toString()), nameparty);
			bpmn.setId(id_description.getLeft());
			bpmn.setDescription(id_description.getRight());
			
			bpmnSet.add(bpmn);
		
		}
		
		//Detect message flow
		Elements collaborationel = doc.getElementsByTag(DotBPMNKeyW.COLLABORATION.toString());

		Set<Pair<FlowNode, FlowNode>> messageflow = new HashSet<Pair<FlowNode, FlowNode>>();
		collaborationel.forEach(c -> {
			for (Element e : c.children()) {
				if (e.tagName().equals(DotBPMNKeyW.MESSAGEFLOW.toString())) {
					Pair<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>, FlowNode> entry = detectSenderReceiver(bpmnSet,
							e.attr(DotBPMNKeyW.SOURCEREF.toString()));
					Pair<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>, FlowNode> exit = detectSenderReceiver(bpmnSet,
							e.attr(DotBPMNKeyW.TARGETREF.toString()));
					messageflow.add(Pair.of(entry.getRight(), exit.getRight()));
				}
			}
		});
		//correctData(bpmnSet);
		return Pair.of(bpmnSet, messageflow);
	}

	private static Bpmn<BpmnControlFlow<FlowNode>, FlowNode> constructBPMN(Element elements) {
		Bpmn<BpmnControlFlow<FlowNode>, FlowNode> bpmn = new Bpmn<BpmnControlFlow<FlowNode>, FlowNode>();
		Set<PETExtendedNode> datanodeSet = detectDataObject(elements.getElementsByTag(DotBPMNKeyW.DATAOBJECTREF.toString()));

		for (Element e : elements.children()) {
			FlowNode f = null;
			if (e.tagName().equals(DotBPMNKeyW.STARTEVENT.toString())) {
				f = new StartEvent();
				f.setTag(BPMNLabel.STARTEVENT);
			} else if (e.tagName().equals(DotBPMNKeyW.ENDEVENT.toString())) {
				f = new EndEvent();
				f.setTag(BPMNLabel.ENDEVENT);
			} else if (e.tagName().equals(DotBPMNKeyW.TASK.toString())) {
				f = new Task();
				f.setTag(BPMNLabel.TASK);
				detectAssociation(e.children(), datanodeSet, f);
			} else if (e.tagName().equals(DotBPMNKeyW.INTERCATCHEVENT.toString())) {
				f = new CatchingEvent();
				f.setTag(BPMNLabel.MESSAGE);
				detectAssociation(e.children(), datanodeSet, f);
			} else if (e.tagName().equals(DotBPMNKeyW.EXCLUSIVEGAT.toString())) {
				f = new AlternativeGateway();
				f.setTag(BPMNLabel.XOR);
			} else if (e.tagName().equals(DotBPMNKeyW.PARALLELGAT.toString())) {
				f = new AlternativeGateway();
				f.setTag(BPMNLabel.AND);
			} else if (e.tagName().equals(DotBPMNKeyW.EVENTBASEDGAT.toString())) {
				f = new AlternativeGateway();
				f.setTag(BPMNLabel.EVENTBASEDG);
			} else {
				continue;
			}
			setIdName(f, e);
			bpmn.addFlowNode(f);
		}
		datanodeSet.forEach(nfn -> bpmn.addNonFlowNode(nfn));

		defineControlFlow(bpmn, elements.getElementsByTag(DotBPMNKeyW.SEQUENCEFLOW.toString()));

		return bpmn;
	}



	/*private static void correctData(Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>> setbpmns) {
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
	}*/

	private static Pair<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>, FlowNode> detectSenderReceiver(
			Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>> bpmnSet, String id) {
		for (Bpmn<BpmnControlFlow<FlowNode>, FlowNode> f : bpmnSet) {
			FlowNode entry;
			if ((entry = getFlowNode(f.getFlowNodes(), id)) != null)
				return Pair.of(f, entry);
		}
		return null;
	}

	//TO-DO: MODIFICARE COME LE PET VENGONO IDENTIFICATE?
	private static void detectAssociation(Elements childrens, Set<PETExtendedNode> datanodeset, FlowNode f) {
		//PET pet = null;
		// Same thing, this should be indipendent from the initial choice
		/*if ((AbstractTranslationAlg.id_op == IDOperaion.SSSHARING.getVal())
				|| (AbstractTranslationAlg.id_op == IDOperaion.ENCRYPTION.getVal())
				|| AbstractTranslationAlg.id_op == IDOperaion.RECONSTRUCTION.getVal())
			pet = detectePet(childrens, datanodeset);*/
		//int coundsharesadditivesss = 0;
		/*if (pet != null) {
			if (pet.getPET().equals(PETLabel.SSSHARING) || pet.getPET().equals(PETLabel.KENCRYPT))
				f.setDescription(pet.getPET().name() + "-" + pet.getIdPet());
			else
				f.setDescription(pet.getPET().name());
		}*/
		for (Element child : childrens) {
			if (child.tagName().equals(DotBPMNKeyW.DATAINPUTASS.toString())) {
				String dataobjref = child.getElementsByTag(DotBPMNKeyW.SOURCEREF.toString()).text();
				datanodeset.stream().filter(p -> getIdDataNode(p).equals(dataobjref))
						.forEach(d -> d.addReadingFlowNode(f));
			} else if (child.tagName().equals(DotBPMNKeyW.DATAOUTPUTASS.toString())) {
				String dataobjref = child.getElementsByTag(DotBPMNKeyW.TARGETREF.toString()).text();
				datanodeset.stream().filter(p -> getIdDataNode(p).equals(dataobjref))
						.forEach(d -> d.addWritingFlowNode(f));
				/*if (pet != null) {
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
				}*/
			}
			continue;
		}
		/*if (coundsharesadditivesss != 0) {
			((SSsharing) pet).setThreshold(coundsharesadditivesss);
			coundsharesadditivesss = 0;
		}*/
	}

	/*private static PET detectePet(Elements childrens, Set<PETExtendedNode> datanodeset) throws JSONException {
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
	/*			return kdecrypt;
			}
		}
		return null;
	}*/

	/*
	 * DataNode composition: ID _ NAME
	 */
	private static String getIdDataNode(DataNode d) {
		int spliti = d.getId().lastIndexOf(d.getName());
		return d.getId().substring(0, spliti);
	}

	//TO-DO FORSE MODIFICARE SE NON SERVE ITERARE SUL PERCORSO PRIMA, ANCORA DA VERIFICARE
	/*
	 * Given the processRef of a participant return its ID, and Decription(name) if there is
	 */
	private static Pair<String,String> retriveCollaborationName(Elements elements, String name) {
		String id = "";
		String description="";
		for(Element e : elements) {
			if (e.attr("processref").equals(name)) {
				id = e.attr(DotBPMNKeyW.ID.toString());
				description = e.attr(DotBPMNKeyW.NAME.toString());
				break;
			}
		}
		return Pair.of(id, description);
	}

	/*
	 * Create a Set of Data Object s.t : ID-Name
	 */
	//private static Map<String, PET> dataobjrefMap = new HashMap<String, PET>();

	/*private static void addtoDaraObjMap(String s, PET p) {
		boolean change = false;
		for (Entry<String, PET> entry : dataobjrefMap.entrySet()) {
			if (entry.getKey().equals(s)) {
				change = true;
			}
		}
		if (!change)
			dataobjrefMap.put(s, p);
	}*/

	//private static Map<String, Pair<Integer, Set<String>>> mapkeygroupid = new HashMap<String, Pair<Integer, Set<String>>>();

	private static Set<PETExtendedNode> detectDataObject(Elements datapobref) {
		Set<PETExtendedNode> datanodeSet = new HashSet<PETExtendedNode>();
		for (Element d : datapobref) {
			String name = d.attr(DotBPMNKeyW.NAME.toString());
			//name = name.replace(".", "_");
			String[] differentnames = name.split(",");
			for (int i = 0; i < differentnames.length; i++) {
				PETExtendedNode dn = new PETExtendedNode();
				//dn.setId(d.attr("id") + differentnames[i]);
				dn.setId(d.attr("id"));
				dn.setName(differentnames[i]);
				datanodeSet.add(dn);
				/*for (Entry<String, PET> entry : dataobjrefMap.entrySet()) {
					if (entry.getKey().equals(differentnames[i]))
						dn.setPET(entry.getValue());
				}*/
				//TO-DO: CONTROLLARE QUESTA PARTE , PROBABILMENTE CAMBIERA'
				/*for (Element child : d.children()) {
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
				}*/
			}
		}
		return datanodeSet;
	}

	private static void setIdName(FlowNode f, Element e) {
		f.setName(e.attr(DotBPMNKeyW.NAME.toString()));
		f.setId(e.attr(DotBPMNKeyW.ID.toString()));
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
			bpmn.addControlFlow(getFlowNode(bpmn.getFlowNodes(), e.attr(DotBPMNKeyW.SOURCEREF.toString())),
					getFlowNode(bpmn.getFlowNodes(), e.attr(DotBPMNKeyW.TARGETREF.toString())));
		}
	}

}
