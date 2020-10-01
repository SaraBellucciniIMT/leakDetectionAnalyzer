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
import rpstTest.Utils;
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
			// System.out.println(triple.toString());
			int size = triple.getMiddle().size();

			DataParameter[] parameters = new DataParameter[size];
			int j = 0;
			for (DataNode dn : triple.getMiddle()) {
				parameters[j] = new DataParameter(mcrl2.getSortData(),
						dataplaceholder.get(dn.getName()).getPlaeholder());
				j++;
			}
			// Update the storage of the right side Process
			if (triple.getLeft().contains(epsilon)) {
				TaskProcess R = getProcessOfTask(triple.getRight());
				triple.getMiddle().forEach(d -> {
					DataParameter dataparameter = new DataParameter(d.getName(), mcrl2.getSortData());
					if (((PETExtendedNode) d).getPET() != null)
						dataparameter.setPrivate(((PETExtendedNode) d).getPET());
					mcrl2.getSortName().addType(d.getName());
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
						DataParameter dataparameter = new DataParameter(d.getName(), mcrl2.getSortData());
						// dataparameter.setId(AbstractProcess.id);
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
		for (DataNode d : triple.getMiddle()) {
			if (isitFirst(node, d)) {
				DataParameter dataparameter = new DataParameter(d.getName(), mcrl2.getSortData());
				if (d.getClass().equals(PETExtendedNode.class) && ((PETExtendedNode) d).hasPET()) {
					mcrl2.getSortData().addPair(Pair.of(d.getName(), ((PETExtendedNode) d).getPET()));
					dataparameter.setPrivate(((PETExtendedNode) d).getPET());
				}
				parametertosend.add(dataparameter);
				mcrl2.getSortName().addType(d.getName());
			} else
				parametertosend
						.add(new DataParameter(mcrl2.getSortData(), dataplaceholder.get(d.getName()).getPlaeholder()));

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
					DataParameter dp = new DataParameter(d.getName(), mcrl2.getSortData());
					mcrl2.getSortName().addType(d.getName());
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
		Buffer sumprocess = new Buffer(parameters, mcrl2.getSortData());
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
			eps = eps + mCRL2.printf(mCRL2.node, mCRL2.emptyf);
			if (j != sumprocess.getInitialParameters().length - 1)
				eps = eps + ",";
		}
		this.mcrl2.addInitSet(sumprocess.getName() + "(" + eps + ")");
	}

	private void generateCommunicationBufferBLocking(Action i, Action o, DataParameter[] parameters) {

		Buffer sumprocess = new Buffer(parameters, mcrl2.getSortData());
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
		Action actionempty = new Action(emptyness + ")");
		Process processempty = new Process(actionempty);
		Process ifp = new Process(Operator.IF, processempty.getName(), outpuprocess.getName());
		ifp.addInsideDef(processempty, outpuprocess);
		sumprocess.setOperant(bufferl, ifp);
		this.mcrl2.addProcess(sumprocess);
		String eps = "";
		for (int j = 0; j < sumprocess.getInitialParameters().length; j++) {
			eps = eps + mCRL2.eps;
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

	private String endcodeENCRYInsideMemory(PartecipantProcess p, String m, DataParameter e) {
		Set<Pair<String, PET>> pair = mcrl2.getSortData().getPrivatePair();
		Set<Integer> pairtocheck = new HashSet<Integer>();
		List<String> childspartecipant = new ArrayList<String>();
		childspartecipant.addAll(mCRL2.childTaskProcess(p.getProcess(), mcrl2, childspartecipant));
		String s = "";
		for (String child : childspartecipant) {
			Action action = mcrl2.getActionByName(child);
			if (!action.getPet().isEmpty()) {
				if (action.getPet().equals(PETLabel.KDECRYPT.name()))
					return s;
			}
		}
		for (Pair<String, PET> t : pair) {
			boolean tocheck = true;
			for (String child : childspartecipant) {
				Action action = mcrl2.getActionByName(child);
				if (action.getPet() != null && action.getPet().equals(PETLabel.KENCRYPT.name())
						&& action.getPETid().equals(String.valueOf(t.getRight().getIdPet()))) {
					tocheck = false;
					break;
				}
			}
			if (tocheck)
				pairtocheck.add(t.getRight().getIdPet());
		}
		if (!pairtocheck.isEmpty()) {
			int size = 0;
			for (int i : pairtocheck) {
				s = s + mCRL2.printf("encryptionviolation", mCRL2.printf(mCRL2.unionf, m, e.toString()),
						String.valueOf(i));
				if (size != pairtocheck.size() - 1)
					s = s + "||";
			}
			s = mCRL2.printifeqn(s, mCRL2.violation + ".delta", "");
		}
		return s;
	}

	/*
	 * Econde ssharing violation action only if your are not creating that shares
	 */
	private void addRecostructionConstraint() {
		Action a = mcrl2.identifyRecostructionTask();
		if (a != null) {
			DataParameter[] dp = a.getParameters();
			String s = "";
				s = s + mCRL2.printf("is_recostructed",
						mCRL2.printf("list2bag", "[" + Utils.organizeParameterAsString(dp) + "],{0:0}"));
				
			String complete = mCRL2.printifeqn(s, mCRL2.recostruct, "") + "<>" + mCRL2.norecostruct;
			TaskProcess t = mcrl2.identifyTaskProcessFromAction(a);
			t.setRecostructionChecking(complete);
		}
	}

	private String encodeSSSPetInsideMemory(PartecipantProcess p, String m, DataParameter e) {
		Set<Pair<String, PET>> pair = mcrl2.getSortData().getPrivatePair();
		Set<PET> pairtocheck = new HashSet<PET>();
		String s = "";
		List<String> childspartecipant = new ArrayList<String>();
		childspartecipant.addAll(mCRL2.childTaskProcess(p.getProcess(), mcrl2, childspartecipant));
		for (String child : childspartecipant) {
			Action action = mcrl2.getActionByName(child);
			if (!action.getPet().isEmpty()) {
				if (action.getPet().equals(PETLabel.SSRECONTRUCTION.name()))
					return s;
			}
		}

		for (Pair<String, PET> t : pair) {
			boolean tocheck = true;
			for (String child : childspartecipant) {
				Action action = mcrl2.getActionByName(child);
				PETLabel petlabel = t.getRight().getPET();
				if (action.getPet() != null && petlabel.equals(PETLabel.SSSHARING)
						&& action.equalPet(petlabel.name(), String.valueOf(t.getRight().getIdPet()))) {
					tocheck = false;
					break;
				}
			}
			if (tocheck)
				pairtocheck.add(t.getRight());
		}
		for (PET tt : pairtocheck) {
			if (!s.isEmpty())
				s = s + "||";
			PETLabel petlabel = tt.getPET();
			if (petlabel.equals(PETLabel.SSSHARING)) {
				s = s + mCRL2.printf("sssharingviolation", String.valueOf(tt.getIdPet()),
						mCRL2.printf(mCRL2.unionf, m.toString(), e.toString()));
			} else if (petlabel.equals(PETLabel.SSCOMPUTATION)) {
				s = s + mCRL2.printf("sscompviolation", String.valueOf(tt.getIdPet()),
						mCRL2.printf(mCRL2.unionf, m.toString(), e.toString()));
			} else if (petlabel.equals(PETLabel.SSRECONTRUCTION)) {
				s = s + "ssrecviolation (" + e + ")";
			}

		}
		if (!s.isEmpty())
			s = "(" + s + ")->VIOLATION.delta";
		return s;

	}

	@Override
	public mCRL2 getSpec() {
		
		tmcrl2 = new HashSet<Tmcrl>();
		for (Bpmn<BpmnControlFlow<FlowNode>, FlowNode> b : bpmn)
			tmcrl2.add(analyzeControlFlow(b));

		mcrl2 = new mCRL2();
		mcrl2.setSortMemory();
		mcrl2.setSortData();
		//How want it to be indipendent from the initial choice, so this will dissappear later on.
		/*if (id_op == IDOperaion.SSSHARING.getVal() || id_op == IDOperaion.ENCRYPTION.getVal()
				|| id_op == IDOperaion.RECONSTRUCTION.getVal()) {
			mcrl2.setSortPName();
			mcrl2.setSortPrivacy();
			mcrl2.getSortData().addType(
					mCRL2.printf(mCRL2.pnode, mCRL2.pv + ":" + mcrl2.getSortPrivacy().getName()) + "?" + mCRL2.is_pn);
			if (id_op == IDOperaion.SSSHARING.getVal() || id_op == IDOperaion.RECONSTRUCTION.getVal()) {
			}
		}*/

		
		tmcrl2.forEach(t -> {
			mcrl2.addProcesses(t.getProcess());
			mcrl2.addAction(t.getActions().toArray(new Action[] {}));
			mcrl2.addAllow(t.getActions().toArray(new Action[] {}));
			mcrl2.addInitSet(t.getFirstProcess().getName());
		});

		analyzeData();
		Set<PartecipantProcess> partecipant = collectPartecipants();
		for (PartecipantProcess p : partecipant)
			generateProcessMemory(p);

		changePartecipants();
		mcrl2.taureduction();
		addConnectionToMemory();
		//if (AbstractTranslationAlg.id_op == IDOperaion.RECONSTRUCTION.getVal())
			//addRecostructionConstraint();
		return mcrl2;
	}

	private void generateProcessMemory(PartecipantProcess p) {
		// sum b:Bool
		DataParameter parbool = new DataParameter(mcrl2.getSortBool());
		Process sumbool = new Process(Action.sumAction(parbool), Operator.SUM);
		// ----
		// sum d:Data
		DataParameter pardata = new DataParameter(mcrl2.getSortMemory());
		Process sumdata = new Process(Action.sumAction(pardata), Operator.SUM);
		// ---
		// s(b,d) also added to the action
		Action s = Action.setTemporaryAction(parbool, pardata);
		Process processs = new Process(s);
		// ---
		Process notcomplete = new Process(Operator.DOT, sumbool.getName(), sumdata.getName(), processs.getName());
		// (.. )-> .. <> ...
		Process negbool = new Process(new Action("(!" + parbool.getPlaeholder() + ")"));

		Action memp = Action.setMemoryAction(mcrl2.getSortMemory());
		p.setActionMemory(memp);
		Process thenp = null;
		if (id_op == IDOperaion.TASK.getVal() || id_op == IDOperaion.PARTICIPANT.getVal()
				|| id_op == IDOperaion.RECONSTRUCTION.getVal())
			thenp = new Process(new Action(mCRL2.printf(notcomplete.getName(),
					mCRL2.printf(mCRL2.unionf, p.retriveDataMemoryName(), pardata.toString()))));
		else if (id_op == IDOperaion.SSSHARING.getVal() || id_op == IDOperaion.ENCRYPTION.getVal()) {
			String encoding = "";
			if (id_op == IDOperaion.SSSHARING.getVal())
				encoding = encodeSSSPetInsideMemory(p, p.retriveDataMemoryName(), pardata);
			else if (id_op == IDOperaion.ENCRYPTION.getVal())
				encoding = endcodeENCRYInsideMemory(p, p.retriveDataMemoryName(), pardata);
			if (encoding.isEmpty())
				thenp = new Process(new Action(
						notcomplete.getName() + "(union(" + p.retriveDataMemoryName() + "," + pardata + "))"));
			else
				thenp = new Process(new Action(encoding + "<>" + notcomplete.getName() + "(union("
						+ p.retriveDataMemoryName() + "," + pardata + "))"));
		}
		mcrl2.addAction(memp, s);
		mcrl2.addAllow(memp);
		Process elsepmep = new Process(memp);
		Process eleserec = new Process(new Action(mCRL2.printf(notcomplete.getName(), p.retriveDataMemoryName())));
		Process elsep = new Process(Operator.DOT, elsepmep.getName(), eleserec.getName());
		elsep.addInsideDef(elsepmep, eleserec);
		Process ifthen = new Process(Operator.IF, negbool.getName(), thenp.getName(), elsep.getName());
		ifthen.addInsideDef(negbool, thenp, elsep);
		// -----
		notcomplete.addChild(ifthen.getName());
		notcomplete.addInsideDef(sumbool, sumdata, processs, ifthen);
		p.setMemory(notcomplete);
		p.setActionToMemory(s.getName());
		mcrl2.addInitSet(
				mCRL2.printf(notcomplete.getName(), Action.setLeftParenthesis() + Action.setRightParenthesis()));
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
		Action codomain = Action.setSendReadAction(new DataParameter(mcrl2.getSortBool()),
				new DataParameter(mcrl2.getSortMemory()));
		for (PartecipantProcess partecipant : collectPartecipants()) {
			Set<String> sendedtomemory = new HashSet<String>();
			List<String> childspartecipant = mCRL2.childTaskProcess(partecipant.getProcess(), mcrl2,
					new ArrayList<String>());
			for (String s : childspartecipant) {
				TaskProcess t = identiyTaskProcessName(s);
				Action output = new Action(partecipant.getActionToMemory(),
						new DataParameter("false", mcrl2.getSortBool()));
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
			Action a = new Action(p.getActionToMemory(), new DataParameter("true", mcrl2.getSortBool()),
					new DataParameter(mCRL2.eps, mcrl2.getSortData()));
			a.setTemporaty();
			Process lastsend = new Process(a);
			newpartecipant.addChild(lastsend.getName());
			newpartecipant.addInsideDef(lastsend);
			newpartecipant.addInsideDef(p.getProcess().getAllInsideDef().toArray(new Process[] {}));
			mcrl2.removePinInitSet(p.getName());
			p.setProcessPartecipant(newpartecipant);
			mcrl2.addInitSet(p.getName());
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
