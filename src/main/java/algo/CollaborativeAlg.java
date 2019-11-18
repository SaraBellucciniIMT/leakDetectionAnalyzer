package algo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.jbpt.pm.DataNode;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.IFlowNode;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.bpmn.BpmnControlFlow;
import org.jbpt.pm.bpmn.Task;
import algo.interpreter.Tmcrl;
import io.PETExtendedNode;
import io.pet.PET;
import io.pet.PETLabel;
import spec.mcrl2obj.AbstractProcess;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.Buffer;
import spec.mcrl2obj.CommunicationFunction;
import spec.mcrl2obj.DataParameter;
import spec.mcrl2obj.Operator;
import spec.mcrl2obj.PartecipantProcess;
import spec.mcrl2obj.Process;
import spec.mcrl2obj.TaskProcess;
import spec.mcrl2obj.mCRL2;

/*
 * 
 */
public class CollaborativeAlg extends AbstractTranslationAlg {

	private Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>> bpmn;
	private Set<Triple<Set<IFlowNode>, Set<DataNode>, IFlowNode>> internalCommList;
	private static final FlowNode epsilon = new Task();

	private Set<Pair<FlowNode, FlowNode>> messages;
	private Set<Tmcrl> tmcrl2;
	private mCRL2 mcrl2;
	
	public CollaborativeAlg(Pair<Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>, Set<Pair<FlowNode, FlowNode>>> pair) {
		this.bpmn = pair.getLeft();
		this.messages = pair.getRight();

	}

	protected void analyzeData() {
		generateInternalCommunicationlist();
		assignPlaceholder();
		for (Triple<Set<IFlowNode>, Set<DataNode>, IFlowNode> triple : internalCommList) {
			//System.out.println(triple.toString());
			int size = triple.getMiddle().size();
			DataParameter[] parameters = new DataParameter[size];
			int j = 0;
			for (DataNode dn : triple.getMiddle()) {
				parameters[j] = new DataParameter(dataplaceholder.get(dn.getName()).getPlaeholder(), getSortEvalData());
				j++;
			}
			// Update the storage of the right side Process
			if (triple.getLeft().contains(epsilon)) {
				TaskProcess R = getProcessOfTask(triple.getRight());
				triple.getMiddle().forEach(d -> {
					DataParameter dataparameter = new DataParameter(d.getName(), getSortEvalData());
					dataparameter.setId(AbstractProcess.id);
					getSortData().addType(d.getName());
					R.addDataToAction(dataparameter);
				});
				continue;
			}
			// System.out.println(triple.toString());
			Action send = null;
			DataParameter[] parametertosend = null;
			for (IFlowNode left : triple.getLeft()) {
				TaskProcess S = getProcessOfTask(left);
				if (send != null) {
					S.addOutputAction(send);
					for (int k = 0; k < parametertosend.length; k++) {
						if (!S.getAction().containsParameter(parametertosend[k]))
							S.getAction().addDataParameter(parametertosend[k]);
					}
					continue;
				}
				parametertosend = createOutputChannel(triple, left);
				if (triple.getRight().equals(epsilon)) {
					triple.getMiddle().forEach(d -> {
						DataParameter dataparameter = new DataParameter(d.getName(), getSortEvalData());
						dataparameter.setId(AbstractProcess.id);
						S.addDataToAction(dataparameter);
					});
					continue;
				}
				// Generate i(e_1,...,e_n)
				Action i = Action.inputAction(parameters);
				// Generate o(e_1,...,e_n)
				Action o = Action.outputAction(parameters);
				// Generate process for the intermediate message event
				TaskProcess R = getProcessOfTask(triple.getRight());
				Process pread = null;
				Process suminput = new Process(Action.sumAction(parameters), Operator.SUM);
				for (PartecipantProcess p : collectPartecipants()) {
					List<String> childs = new ArrayList<String>();
					childs.addAll(mCRL2.childTaskProcess(p.getProcess(), mcrl2, childs));
					if (childs.contains(S.getAction().getName()) && childs.contains(R.getAction().getName())) {
						generateCommunicationBufferNonBLocking(i, o, parameters);
						break;
					} else if (childs.contains(S.getAction().getName()) && !childs.contains(R.getAction().getName())
							|| (childs.contains(R.getAction().getName())
									&& !childs.contains(S.getAction().getName()))) {
						generateCommunicationBufferBLocking(i, o, parameters);
						break;
					}
				}

				send = Action.outputAction(parametertosend);
				// Update internal memory
				for (int k = 0; k < parametertosend.length; k++) {
					if (!S.getAction().containsParameter(parametertosend[k]))
						S.getAction().addDataParameter(parametertosend[k]);
				}
				S.addOutputAction(send);
				Action read = Action.inputAction(parameters);
				pread = new Process(read);
				this.mcrl2.addCommunicaitonFunction(createSendReadCommunication(send, i));
				this.mcrl2.addCommunicaitonFunction(createSendReadCommunication(o, read));
				for (DataParameter d : parameters) {
					if (!R.getAction().containsParameter(d))
						R.addDataToAction(d);
				}
				Process seqsuminput = new Process(Operator.DOT, suminput.getName(), pread.getName());
				R.addInputAction(seqsuminput, suminput, pread);
			}
		}
	}

	private CommunicationFunction createSendReadCommunication(Action a, Action b) {
		mcrl2.addAction(a, b);
		Action read = Action.setSendReadAction(a.getParameters());
		addToActionAllowHide(read);
		return new CommunicationFunction(read, a.getName(), b.getName());
	}

	private void addToActionAllowHide(Action a) {
		mcrl2.addAction(a);
		mcrl2.addAllow(a);
		mcrl2.addHide(a);
	}

	private DataParameter[] createOutputChannel(Triple<Set<IFlowNode>, Set<DataNode>, IFlowNode> triple,
			IFlowNode node) {
		List<DataParameter> parametertosend = new ArrayList<DataParameter>();
		for(DataNode d : triple.getMiddle()) {
			if (isitFirst(node, d)) {
				DataParameter dataparameter = new DataParameter(d.getName(), getSortEvalData());
				if (d.getClass().equals(PETExtendedNode.class) && ((PETExtendedNode) d).hasPET()) {
					getSortEvalData().addTriple(Triple.of(d.getName(), ((PETExtendedNode) d).getPET(),
							((PETExtendedNode) d).getPET().getID_protection()));
					dataparameter.setId(String.valueOf(((PETExtendedNode) d).getPET().getID_protection()));
					dataparameter.setPrivate();
				}else {
					dataparameter.setId(AbstractProcess.id);
					getSortEvalData().addTriple(Triple.of(d.getName(), null, mcrl2.identifyMyParticipant(getProcessOfTask(node)).getIdParty()));
				}	
				parametertosend.add(dataparameter);
				getSortData().addType(d.getName());
			} else
				parametertosend
						.add(new DataParameter(dataplaceholder.get(d.getName()).getPlaeholder(), getSortEvalData()));

		}
		return parametertosend.toArray(new DataParameter[] {});
	}

	/*
	 * String : name of the data object DataParameter : placeholder for that data
	 * object
	 */
	private Map<String, DataParameter> dataplaceholder;

	// Gives a fixed placeholder to each dataparameter

	private void assignPlaceholder() {
		this.dataplaceholder = new HashMap<String, DataParameter>();
		for (Triple<Set<IFlowNode>, Set<DataNode>, IFlowNode> triple : internalCommList) {
			triple.getMiddle().forEach(d -> {
				if (!dataplaceholder.containsKey(d.getName())) {
					DataParameter dp = new DataParameter(d.getName(), getSortEvalData());
					getSortData().addType(d.getName());
					dp.setPlaceHolder();
					dataplaceholder.put(d.getName(), dp);
				}
			});
		}
	}

	private boolean isitFirst(IFlowNode node, DataNode data) {
		for (Triple<Set<IFlowNode>, Set<DataNode>, IFlowNode> triple : internalCommList) {
			for (DataNode datanode : triple.getMiddle()) {
				if (datanode.getName().equals(data.getName())) {
					if (triple.getRight().equals(node) && !triple.getLeft().contains(epsilon))
						return false;
				}
			}
		}
		return true;
	}

	private TaskProcess getProcessOfTask(IFlowNode f) {
		for (Tmcrl t : tmcrl2) {
			TaskProcess task;
			if ((task = t.getProcessOfTask(f)) != null)
				return task;
		}
		return null;
	}

	private void generateCommunicationBufferNonBLocking(Action i, Action o, DataParameter[] parameters) {
		Buffer sumprocess = new Buffer(parameters, getSortEvalData());
		sumprocess.setBufferName();
		TaskProcess bufferl = new TaskProcess();
		// generate i(e1,...,e_n)
		// Generate the sum : e1,...en:Data
		Process sum = new Process(Action.sumAction(parameters), Operator.SUM);
		Process input = new Process(i);
		Process seqinputsum = new Process(Operator.DOT, sum.getName(), input.getName());
		bufferl.addInputAction(seqinputsum, input, sum);

		Action a = new Action(o.getName(), sumprocess.getInitialParameters());
		this.mcrl2.addAction(a);
		Process bufferr = new Process(a);

		sumprocess.setOperant(bufferl, bufferr);
		this.mcrl2.addProcess(sumprocess);
		String eps = "";
		for (int j = 0; j < sumprocess.getInitialParameters().length; j++) {
			eps = eps + AbstractTranslationAlg.empty;
			if (j != sumprocess.getInitialParameters().length - 1)
				eps = eps + ",";
		}
		this.mcrl2.addInitSet(sumprocess.getName() + "(" + eps + ")");
	}

	private void generateCommunicationBufferBLocking(Action i, Action o, DataParameter[] parameters) {

		Buffer sumprocess = new Buffer(parameters, getSortEvalData());
		sumprocess.setBufferName();
		TaskProcess bufferl = new TaskProcess();
		// generate i(e1,...,e_n)
		// Generate the sum : e1,...en:Data
		Process sum = new Process(Action.sumAction(parameters), Operator.SUM);
		Process input = new Process(i);
		Process seqinputsum = new Process(Operator.DOT, sum.getName(), input.getName());
		bufferl.addInputAction(seqinputsum, input, sum);

		Action a = new Action(o.getName(), sumprocess.getInitialParameters());
		this.mcrl2.addAction(a);
		Process outpuprocess = new Process(a);
		String emptyness = "(!empty(" + sumprocess.getInitialParameters()[0].getPlaeholder() + ")";
		for (int j = 1; j < sumprocess.getInitialParameters().length; j++)
			emptyness = emptyness + "&& !empty(" + sumprocess.getInitialParameters()[j].getPlaeholder() + ")";
		// Action actionempty = new Action("!empty", sumprocess.getInitialParameters());
		Action actionempty = new Action(emptyness + ")");
		Process processempty = new Process(actionempty);
		Process ifp = new Process(Operator.IF, processempty.getName(), outpuprocess.getName());
		ifp.addInsideDef(processempty, outpuprocess);
		sumprocess.setOperant(bufferl, ifp);
		this.mcrl2.addProcess(sumprocess);
		String eps = "";
		for (int j = 0; j < sumprocess.getInitialParameters().length; j++) {
			eps = eps + AbstractTranslationAlg.empty;
			if (j != sumprocess.getInitialParameters().length - 1)
				eps = eps + ",";
		}
		this.mcrl2.addInitSet(sumprocess.getName() + "(" + eps + ")");
	}

	// What is in output from a Intermediate Message Event is the data that was
	// sended
	private Set<DataNode> findData(IFlowNode f, Set<Triple<IFlowNode, DataNode, IFlowNode>> tmpinternalCommList) {
		Set<DataNode> set = new HashSet<DataNode>();
		for (Triple<IFlowNode, DataNode, IFlowNode> triple : tmpinternalCommList) {
			if (triple.getLeft().equals(f)) {
				set.add(triple.getMiddle());
			}
		}
		return set;
	}

	// Return the flownode that correspond to the input vertix in that bpmn model

	private void generateInternalCommunicationlist() {
		epsilon.setTag("epsilon");
		epsilon.setName("epsilon");
		Set<Triple<IFlowNode, DataNode, IFlowNode>> tmpInternalCommList = new HashSet<Triple<IFlowNode, DataNode, IFlowNode>>();
		bpmn.forEach(b -> {
			for (DataNode n : b.getDataNodes()) {

				Collection<IFlowNode> writingnodes = n.getWritingFlowNodes();
				Collection<IFlowNode> readnodes = n.getReadingFlowNodes();
				if (!writingnodes.isEmpty() && !readnodes.isEmpty()) {
					writingnodes.forEach(w -> {
						readnodes.forEach(r -> tmpInternalCommList.add(Triple.of(w, n, r)));
					});
				} else if (writingnodes.isEmpty() && !readnodes.isEmpty()) {
					readnodes.forEach(r -> tmpInternalCommList.add(Triple.of(epsilon, n, r)));
				} else if (!writingnodes.isEmpty() && readnodes.isEmpty()) {
					writingnodes.forEach(w -> tmpInternalCommList.add(Triple.of(w, n, epsilon)));
				}
			}
		});
		messages.forEach(pair -> {
			Set<DataNode> set = findData(pair.getRight(), tmpInternalCommList);
			for (DataNode d : set) {
				tmpInternalCommList.add(Triple.of(pair.getLeft(), d, pair.getRight()));
			}
		});
		combiningbyData(combiningbyDataReceiver(tmpInternalCommList));
	}

	/*
	 * It combines the triple that have the same Sender and Receiver
	 */
	private void combiningbyData(Set<Triple<Set<IFlowNode>, Set<DataNode>, IFlowNode>> tmpintermalcommlist) {
		this.internalCommList = new HashSet<Triple<Set<IFlowNode>, Set<DataNode>, IFlowNode>>();
		for (Triple<Set<IFlowNode>, Set<DataNode>, IFlowNode> triple : tmpintermalcommlist) {
			boolean find = false;
			for (Triple<Set<IFlowNode>, Set<DataNode>, IFlowNode> t : this.internalCommList) {
				if (t.getRight().equals(triple.getRight()) && t.getLeft().equals(triple.getLeft())) {
					t.getMiddle().addAll(triple.getMiddle());
					find = true;
					break;
				}
			}
			if (!find)
				this.internalCommList.add(triple);
		}
	}

	/*
	 * It combines the triples that have the same Data and the same Receiver
	 */
	private Set<Triple<Set<IFlowNode>, Set<DataNode>, IFlowNode>> combiningbyDataReceiver(
			Set<Triple<IFlowNode, DataNode, IFlowNode>> set) {
		Set<Triple<IFlowNode, DataNode, IFlowNode>> copyset = new HashSet<Triple<IFlowNode, DataNode, IFlowNode>>(set);
		Set<Triple<Set<IFlowNode>, Set<DataNode>, IFlowNode>> tmpintermalcommlist = new HashSet<Triple<Set<IFlowNode>, Set<DataNode>, IFlowNode>>();
		for (Triple<IFlowNode, DataNode, IFlowNode> entry : set) {
			DataNode d = entry.getMiddle();
			if (!copyset.contains(entry))
				continue;
			Set<IFlowNode> senderset = new HashSet<IFlowNode>();
			senderset.add(entry.getLeft());
			Set<DataNode> datanodeset = new HashSet<DataNode>();
			datanodeset.add(entry.getMiddle());
			for (Triple<IFlowNode, DataNode, IFlowNode> entry2 : set) {
				if (!entry.equals(entry2) && d.equals(entry2.getMiddle())
						&& entry.getRight().equals(entry2.getRight())) {
					senderset.add(entry2.getLeft());
					copyset.remove(entry2);
				}
			}
			tmpintermalcommlist.add(Triple.of(senderset, datanodeset, entry.getRight()));
		}
		return tmpintermalcommlist;
	}

	/*
	 * Econde ssharing violation action only if your are not creating that shares
	 */
	private String encodePetInsideMemory(PartecipantProcess p, int id_party, String m, DataParameter e) {
		Set<Triple<String, PET, Integer>> triple = getSortEvalData().getPrivateTriple();
		Set<Triple<String, PET, Integer>> tripletocheck = getSortEvalData().getPrivateTriple();
		String s = "";
		List<String> childspartecipant = new ArrayList<String>();
		childspartecipant.addAll(mCRL2.childTaskProcess(p.getProcess(), mcrl2, childspartecipant));
		for (String child : childspartecipant) {
			Action action = mcrl2.getActionByName(child);
			if (!action.getPet().isEmpty()) {
				String[] petstring = action.getPet().split("-");
				if (petstring[0].equals(PETLabel.SSRECONTRUCTION.name()))
					return s;
			}
		}
		
		for (Triple<String, PET, Integer> t : triple) {
			for (String child : childspartecipant) {
				Action action = mcrl2.getActionByName(child);
				PETLabel name = t.getMiddle().getPET();
				if (action.getPet() != null && name.equals(PETLabel.SSSHARING) && action.equalPet(t.getMiddle())) {
					tripletocheck.remove(t);
					break;
				}
			}
		}
		List<Integer> id_alreadychecked = new ArrayList<Integer>();
		for (Triple<String, PET, Integer> tt : tripletocheck) {
			if (!id_alreadychecked.contains(tt.getRight())) {
				if (!s.isEmpty())
					s = s + "||";
				else
					s = "(";
				s = s + "sssharingviolation(" + tt.getRight() + ",union(" + m + "," + e + "),"
						+ tt.getMiddle().getTreshold() + ")";
				id_alreadychecked.add(tt.getRight());
			}
		}

		if (!s.isEmpty())
			s = s + ")->VIOLATION.delta";
		return s;
	}

	@Override
	public mCRL2 getSpec(int id_op) {
		AbstractTranslationAlg.id_op = id_op;
		tmcrl2 = new HashSet<Tmcrl>();
		for (Bpmn<BpmnControlFlow<FlowNode>, FlowNode> b : bpmn)
			tmcrl2.add(analyzeControlFlow(b));

		mcrl2 = new mCRL2(id_op);
		tmcrl2.forEach(t -> {
			mcrl2.addProcesses(t.getProcess());
			mcrl2.addAction(t.getActions().toArray(new Action[] {}));
			mcrl2.addAllow(t.getActions().toArray(new Action[] {}));
			mcrl2.addInitSet(t.getFirstProcess().getNameInit());
			mcrl2.addSort(getSortData(), getSortMemory(), getSortBool(), getSortEvalData());
		});

		analyzeData();
		Set<PartecipantProcess> partecipant = collectPartecipants();
		for (PartecipantProcess p : partecipant)
			generateProcessMemory(p);
		changePartecipants();
		// checkSensibleData();
		mcrl2.taureduction();
		addConnectionToMemory();
		return mcrl2;
	}

	private void generateProcessMemory(PartecipantProcess p) {
		// sum b:Bool
		DataParameter parbool = new DataParameter(getSortBool());
		Process sumbool = new Process(Action.sumAction(parbool), Operator.SUM);
		// ----
		// sum d:Data
		DataParameter pardata = new DataParameter(getSortMemory());
		Process sumdata = new Process(Action.sumAction(pardata), Operator.SUM);
		// ---
		// s(b,d) also added to the action
		Action s = Action.setTemporaryAction(parbool, pardata);
		Process processs = new Process(s);
		// ---
		Process notcomplete = new Process(Operator.DOT, sumbool.getName(), sumdata.getName(), processs.getName());
		// (.. )-> .. <> ...
		Process negbool = new Process(new Action("(!" + parbool.getPlaeholder() + ")"));

		Action memp = Action.setMemoryAction(getSortMemory());
		p.setActionMemory(memp);
		Process thenp = null;
		if (id_op == IDOperaion.TASK.getVal() || id_op == IDOperaion.PARTICIPANT.getVal())
			thenp = new Process(new Action(notcomplete.getName() + "(union(" + p.retriveDataMemoryName() + "," + pardata
					+ ")," + AbstractProcess.id + ")"));
		else if (id_op == IDOperaion.SSSHARING.getVal()) {
			String encoding = encodePetInsideMemory(p, p.getIdParty(), p.retriveDataMemoryName(), pardata);
			if (encoding.isEmpty())
				thenp = new Process(new Action(notcomplete.getName() + "(union(" + p.retriveDataMemoryName() + ","
						+ pardata + ")," + AbstractProcess.id + ")"));
			else
				thenp = new Process(new Action(encoding + "<>" + notcomplete.getName() + "(union("
						+ p.retriveDataMemoryName() + "," + pardata + ")," + AbstractProcess.id + ")"));
		}
		mcrl2.addAction(memp, s);
		mcrl2.addAllow(memp);
		Process elsepmep = new Process(memp);
		Process eleserec = new Process(
				new Action(notcomplete.getName() + "(" + p.retriveDataMemoryName() + "," + AbstractProcess.id + ")"));
		Process elsep = new Process(Operator.DOT, elsepmep.getName(), eleserec.getName());
		elsep.addInsideDef(elsepmep, eleserec);
		Process ifthen = new Process(Operator.IF, negbool.getName(), thenp.getName(), elsep.getName());
		ifthen.addInsideDef(negbool, thenp, elsep);
		// -----
		notcomplete.addChild(ifthen.getName());
		notcomplete.addInsideDef(sumbool, sumdata, processs, ifthen);
		p.setMemory(notcomplete);
		p.setActionToMemory(s.getName());
		
		mcrl2.addInitSet(notcomplete.getName() + "("+Action.setLeftParenthesis() + Action.setRightParenthesis()+"," + p.getIdParty() + ")");
	}

	// Identify the task process using is task/activity name
	public TaskProcess identiyTaskProcessName(String name) {
		Set<TaskProcess> tasks = getTaskProcessesInsideProcesses();
		for (TaskProcess t : tasks) {
			if (t.getAction() != null && t.getAction().getName().equalsIgnoreCase(name))
				return t;
		}
		return null;
	}

	// Qui è definito la dimensione massima che la memoria raggiungerà
	private void addConnectionToMemory() {
		Action codomain = Action.setSendReadAction(new DataParameter(getSortBool()),
				new DataParameter(getSortMemory()));
		for (PartecipantProcess partecipant : collectPartecipants()) {
			Set<String> sendedtomemory = new HashSet<String>();
			List<String> childspartecipant = mCRL2.childTaskProcess(partecipant.getProcess(), mcrl2,
					new ArrayList<String>());
			for (String s : childspartecipant) {
				TaskProcess t = identiyTaskProcessName(s);
				Action output = new Action(partecipant.getActionToMemory(), new DataParameter("false", getSortBool()));
				output.setTemporaty();
				for (DataParameter d : t.getAction().getParameters()) {
					output.addDataParameter(d);
					t.addOutputAction(output);
					String myData = "";
					for (Entry<String, DataParameter> entry : dataplaceholder.entrySet()) {
						if (entry.getKey().equals(d.getName())
								|| entry.getValue().getPlaeholder().equals(d.getName())) {
							myData = entry.getKey();
							break;
						}
					}
					// defining the maximum memory of a participant
					if (!sendedtomemory.contains(myData))
						sendedtomemory.add(myData);
				}
			}
			partecipant.setMaxDim(sendedtomemory.size());
			mcrl2.addCommunicaitonFunction(new CommunicationFunction(codomain, partecipant.getActionToMemory(),
					partecipant.getActionToMemory()));
		}
		mcrl2.addAction(codomain);
	}

	private Set<TaskProcess> getTaskProcessesInsideProcesses() {
		Set<TaskProcess> taskprocessset = new HashSet<TaskProcess>();
		mcrl2.getProcesses().forEach(tp -> {
			if (tp.getClass().equals(TaskProcess.class))
				taskprocessset.add((TaskProcess) tp);
		});
		return taskprocessset;
	}

	private void changePartecipants() {
		for (PartecipantProcess p : collectPartecipants()) {
			Process newpartecipant = new Process(Operator.DOT);
			for (int i = 0; i < p.getProcess().getLength(); i++)
				newpartecipant.addChild(p.getProcess().getChildName(i));
			Action a = new Action(p.getActionToMemory(), new DataParameter("true", getSortBool()),
					new DataParameter(AbstractTranslationAlg.empty, getSortEvalData()));
			a.setTemporaty();
			Process lastsend = new Process(a);
			newpartecipant.addChild(lastsend.getName());
			newpartecipant.addInsideDef(lastsend);
			newpartecipant.addInsideDef(p.getProcess().getAllInsideDef().toArray(new Process[] {}));
			mcrl2.removePinInitSet(p.getNameInit());
			p.setProcessPartecipant(newpartecipant);
			mcrl2.addInitSet(p.getNameInit());
			mcrl2.addProcess(p);

		}

	}

	/*
	 * Partecipants are always Process because are such that P = P'.P''.P'''. ... .
	 * a(true,eps)
	 */
	private Set<PartecipantProcess> collectPartecipants() {
		Set<PartecipantProcess> partecipants = new HashSet<PartecipantProcess>();
		for (AbstractProcess ap : mcrl2.getProcesses()) {
			if (ap.getClass().equals(PartecipantProcess.class))
				partecipants.add((PartecipantProcess) ap);
		}

		return partecipants;
	}

}
