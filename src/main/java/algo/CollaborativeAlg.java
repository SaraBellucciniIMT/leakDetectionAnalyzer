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
import io.BPMNLabel;
import io.PETExtendedNode;
import io.pet.PET;
import spec.mcrl2obj.AbstractProcess;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.Buffer;
import spec.mcrl2obj.CommunicationFunction;
import spec.mcrl2obj.DataParameter;
import spec.mcrl2obj.Operator;
import spec.mcrl2obj.PartecipantProcess;
import spec.mcrl2obj.Process;
import spec.mcrl2obj.StructSort;
import spec.mcrl2obj.TaskProcess;
import spec.mcrl2obj.mCRL2;

/*
 * 
 */
public class CollaborativeAlg extends AbstractTranslationAlg {

	private Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>> bpmn;
	private Set<Triple<IFlowNode, Set<DataNode>, IFlowNode>> internalCommList;
	private static final FlowNode epsilon = new Task();

	private Set<Pair<FlowNode, FlowNode>> messages;
	private Set<Tmcrl> tmcrl2;
	private mCRL2 mcrl2;

	public CollaborativeAlg(Pair<Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>, Set<Pair<FlowNode, FlowNode>>> pair) {
		this.bpmn = pair.getLeft();
		this.messages = pair.getRight();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see algo.AbstractTranslationAlg#analyzeData()
	 */
	protected void analyzeData() {
		generateInternalCommunicationlist();
		assignPlaceholder();
		messages.forEach(pair -> this.internalCommList
				.add(Triple.of(pair.getLeft(), findData(pair.getRight()), pair.getRight())));
		for (Triple<IFlowNode, Set<DataNode>, IFlowNode> triple : internalCommList) {
			
			int size = triple.getMiddle().size();
			DataParameter[] parameters = new DataParameter[size];
			int j = 0;
			for (DataNode dn : triple.getMiddle()) {
				parameters[j] = parameters[j] = new DataParameter(dataplaceholder.get(dn.getName()).getPlaeholder(),
						getSortData());
				j++;
			}

			// Update the storage of the right side Process
			if (triple.getLeft().equals(epsilon)) {
				TaskProcess R = getProcessOfTask(triple.getRight());
				triple.getMiddle().forEach(d -> {
					R.addDataToAction(new DataParameter(d.getName(), getSortData()));
				});
				continue;
			}
			
			if(triple.getRight().equals(epsilon)) {
				TaskProcess S = getProcessOfTask(triple.getLeft());
				triple.getMiddle().forEach(d->{
					S.addDataToAction(new DataParameter(d.getName(),getSortData()));
				});
				continue;
			}

			// Generate i(e_1,...,e_n)
			Action i = Action.inputAction(parameters);
			// Generate o(e_1,...,e_n)
			Action o = Action.outputAction(parameters);

			// Generate process for the intermediate message event
			TaskProcess S = getProcessOfTask(triple.getLeft());
			TaskProcess R = getProcessOfTask(triple.getRight());
			Process pread = null;
			Process suminput = new Process(Action.sumAction(parameters), Operator.SUM);

			if (/*triple.getLeft().getTag().equals(BPMNLabel.TASK) &&*/ triple.getRight().getTag().equals(BPMNLabel.TASK)) {
				// Generate buffer and add the buffer to the init set
				generateCommunicationBuffer(i, o, parameters);

				DataParameter[] parametertosend = createOutputChannel(triple);
				Action send = Action.outputAction(parametertosend);
				for (int k = 0; k < parametertosend.length; k++) {
					if (!S.getAction().containsParameter(parametertosend[k]))
						S.getAction().addDataParameter(parametertosend[k]);
				}
				S.addOutputAction(send);
				Action read = Action.inputAction(parameters);
				pread = new Process(read);

				this.mcrl2.addCommunicaitonFunction(createSendReadCommunication(send, i));
				this.mcrl2.addCommunicaitonFunction(createSendReadCommunication(o, read));

			} /*else if (triple.getLeft().getTag().equals(BPMNLabel.MESSAGE)) {
				if (!mcrl2.getInitSet().contains(S.getName()))
					mcrl2.addInitSet(S.getName());
				// parameters.forEach(par -> R.addDataToAction(par));
				pread = new Process(i);
				for (int k = 0; k < parameters.length; k++) {
					if (!S.getAction().containsParameter(parameters[k]))
						S.getAction().addDataParameter(parameters[k]);
				}
				S.addOutputAction(o);
				this.mcrl2.addCommunicaitonFunction(createSendReadCommunication(o, i));

			} */else if (triple.getRight().getTag().equals(BPMNLabel.MESSAGE)) {
				if (!mcrl2.getInitSet().contains(R.getName()))
					mcrl2.addInitSet(R.getName());
				pread = new Process(i);
				DataParameter[] parametertosend = createOutputChannel(triple);
				Action send = Action.outputAction(parametertosend);
				S.addOutputAction(send);
				for (int k = 0; k < parametertosend.length; k++) {
					if (!S.getAction().containsParameter(parametertosend[k]))
						S.getAction().addDataParameter(parametertosend[k]);
				}
				this.mcrl2.addCommunicaitonFunction(createSendReadCommunication(i, send));
			}

			for (DataParameter d : parameters) {
				if (!R.getAction().containsParameter(d))
					R.addDataToAction(d);
			}
			Process seqsuminput = new Process(Operator.DOT, suminput.getName(), pread.getName());
			R.addInputAction(seqsuminput, suminput, pread);

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

	private DataParameter[] createOutputChannel(Triple<IFlowNode, Set<DataNode>, IFlowNode> triple) {
		List<DataParameter> parametertosend = new ArrayList<DataParameter>();
		triple.getMiddle().forEach(d -> {
			if (isitFirst(triple.getLeft(), d)) {
				parametertosend.add(new DataParameter(d.getName(), getSortData()));
				getSortData().addType(d.getName());
			} else
				parametertosend.add(new DataParameter(dataplaceholder.get(d.getName()).getPlaeholder(), getSortData()));

		});
		return parametertosend.toArray(new DataParameter[] {});
	}

	/*
	 * String : name of the data object DataParameter : placeholder for that
	 * data object
	 */
	private Map<String, DataParameter> dataplaceholder;

	// Gives a fixed placeholder to each dataparameter

	private void assignPlaceholder() {
		this.dataplaceholder = new HashMap<String, DataParameter>();
		for (Triple<IFlowNode, Set<DataNode>, IFlowNode> triple : internalCommList) {
			triple.getMiddle().forEach(d -> {
				if (!dataplaceholder.containsKey(d.getName())) {
					DataParameter dp = new DataParameter(d.getName(), getSortData());
					getSortData().addType(d.getName());
					dp.setPlaceHolder();
					dataplaceholder.put(d.getName(), dp);
				}
			});
		}
	}

	private boolean isitFirst(IFlowNode node, DataNode data) {
		for (Triple<IFlowNode, Set<DataNode>, IFlowNode> triple : internalCommList) {

			for (DataNode datanode : triple.getMiddle()) {
				if (datanode.getName().equals(data.getName())) {
					if (triple.getRight().equals(node) && !triple.getLeft().equals(epsilon)) {
						return false;
					}
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

	private void generateCommunicationBuffer(Action i, Action o, DataParameter[] parameters) {

		TaskProcess buffer = new TaskProcess();
		// generate i(e1,...,e_n)
		// Generate the sum : e1,...en:Data
		Process sum = new Process(Action.sumAction(parameters), Operator.SUM);
		Process input = new Process(i);

		Process seqinputsum = new Process(Operator.DOT, sum.getName(), input.getName());
		buffer.addInputAction(seqinputsum, input, sum);
		buffer.addOutputAction(o);
		buffer.setBufferName();
		Action a = new Action(o.getName(), new DataParameter(StructSort.empty, getSortData()));
		this.mcrl2.addAction(a);
		Process p = new Process(a);

		Buffer sumprocess = new Buffer(buffer, p);
		sumprocess.setBufferName();
		this.mcrl2.addProcess(sumprocess);
		this.mcrl2.addInitSet(buffer.getName());
	}

	// What is in output from a Intermediate Message Event is the data that was
	// sended
	private Set<DataNode> findData(IFlowNode f) {
		Set<DataNode> set = new HashSet<DataNode>();
		for (Triple<IFlowNode, Set<DataNode>, IFlowNode> triple : internalCommList) {
			if (triple.getLeft().equals(f)) {
				set.addAll(triple.getMiddle());
			}
		}
		return set;
	}

	// Return the flownode that correspond to the input vertix in that bpmn model

	private void generateInternalCommunicationlist() {
		epsilon.setTag("epsilon");
		epsilon.setName("epsilon");
		this.internalCommList = new HashSet<Triple<IFlowNode, Set<DataNode>, IFlowNode>>();
		bpmn.forEach(b -> {
			for (DataNode n : b.getDataNodes()) {
				Collection<IFlowNode> writingnodes = n.getWritingFlowNodes();
				Collection<IFlowNode> readnodes = n.getReadingFlowNodes();
				if(!writingnodes.isEmpty() && !readnodes.isEmpty()) {
				writingnodes.forEach(w -> {
					readnodes.forEach(r -> {
						Triple<IFlowNode, Set<DataNode>, IFlowNode> triple;
						if ((triple = existingWriteReadCouple(w, r)) != null)
							triple.getMiddle().add(n);
						else {
							Set<DataNode> dataset = new HashSet<DataNode>();
							dataset.add(n);
							internalCommList.add(Triple.of(w, dataset, r));
						}
					});
				});
				}
				else if (writingnodes.isEmpty() && !readnodes.isEmpty()) {
					readnodes.forEach(r -> {
						Triple<IFlowNode, Set<DataNode>, IFlowNode> triple = existingWriteReadCouple(epsilon, r);
						if (triple != null)
							triple.getMiddle().add(n);
						else {
							Set<DataNode> dataset = new HashSet<DataNode>();
							dataset.add(n);
							internalCommList.add(Triple.of(epsilon, dataset, r));
						}
					});

				}else if(!writingnodes.isEmpty() && readnodes.isEmpty()) {
					writingnodes.forEach(w -> {
						Triple<IFlowNode, Set<DataNode>, IFlowNode> triple = existingWriteReadCouple(w, epsilon);
						if (triple != null)
							triple.getMiddle().add(n);
						else {
							Set<DataNode> dataset = new HashSet<DataNode>();
							dataset.add(n);
							internalCommList.add(Triple.of(w, dataset, epsilon));
						}
					});
				}
			}
		});
	}

	/*
	 * Check if already exist a couple send-receive that send a different data w.r.t
	 * to the one that we are considering now
	 */
	private Triple<IFlowNode, Set<DataNode>, IFlowNode> existingWriteReadCouple(IFlowNode writer, IFlowNode reader) {
		for (Triple<IFlowNode, Set<DataNode>, IFlowNode> t : internalCommList) {
			if (t.getLeft().equals(writer) && t.getRight().equals(reader))
				return t;
		}
		return null;
	}

	private void checkSensibleData() {
		Map<PET, Set<String>> map = new HashMap<PET, Set<String>>();
		for (Triple<IFlowNode, Set<DataNode>, IFlowNode> triple : internalCommList) {
			for (DataNode data : triple.getMiddle()) {
				String dataname = data.getName().replace(" ", "_");
				if (data.getClass().equals(PETExtendedNode.class) && ((PETExtendedNode) data).getPET() != null)

					if (!map.containsKey(((PETExtendedNode) data).getPET())) {
						Set<String> set = new HashSet<String>();
						set.add(dataname);
						map.put(((PETExtendedNode) data).getPET(), set);
					} else
						map.get(((PETExtendedNode) data).getPET()).add(dataname);
			}
		}
		this.mcrl2.setSensibleData(map);
	}

	@Override
	public mCRL2 getSpec() {
		tmcrl2 = new HashSet<Tmcrl>();
		for (Bpmn<BpmnControlFlow<FlowNode>, FlowNode> b : bpmn)
			tmcrl2.add(analyzeControlFlow(b));

		mcrl2 = new mCRL2();
		tmcrl2.forEach(t -> {
			mcrl2.addProcesses(t.getProcess());
			mcrl2.addAction(t.getActions().toArray(new Action[] {}));
			mcrl2.addAllow(t.getActions().toArray(new Action[] {}));
			mcrl2.addInitSet(t.getFirstProcess());
			mcrl2.addSort(getSortData(), getSortMemory(), getSortBool());
		});

		Set<PartecipantProcess> partecipant = collectPartecipants();
		for (PartecipantProcess p : partecipant)
			generateProcessMemory(p);
		changePartecipants();
		analyzeData();
		checkSensibleData();
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
		DataParameter pardata = new DataParameter(getSortData());
		Process sumdata = new Process(Action.sumAction(pardata), Operator.SUM);
		// ---
		// s(b,d) also added to the action
		Action s = Action.setTemporaryAction(parbool, pardata);
		Process processs = new Process(s);
		// ---
		Process notcomplete = new Process(Operator.DOT, sumbool.getName(), sumdata.getName(), processs.getName());
		// (.. )-> .. <> ...
		Process negbool = new Process(new Action("(!" + parbool.getName() + ")"));
		Process thenp = new Process(new Action(notcomplete.getName() + "(union({" + pardata + "},m))"));
		Action memp = Action.setMemoryAction(getSortMemory());
		p.setActionMemory(memp);
		mcrl2.addAction(memp, s);
		mcrl2.addAllow(memp);
		Process elsepmep = new Process(memp);
		Process eleserec = new Process(new Action(notcomplete.getName() + "(m)"));
		Process elsep = new Process(Operator.DOT, elsepmep.getName(), eleserec.getName());
		elsep.addInsideDef(elsepmep, eleserec);
		Process ifthen = new Process(Operator.IF, negbool.getName(), thenp.getName(), elsep.getName());
		ifthen.addInsideDef(negbool, thenp, elsep);
		// -----
		notcomplete.addChild(ifthen.getName());
		notcomplete.addInsideDef(sumbool, sumdata, processs, ifthen);
		p.setMemory(notcomplete);
		p.setActionToMemory(s.getName());
		mcrl2.addInitSet(notcomplete.getName() + "({})");			
	}

	// Identify the task process using is task/activity name
	public TaskProcess identiyTaskProcessName(String name) {
		Set<TaskProcess> tasks = getTaskProcessesInsideProcesses();
		for (TaskProcess t : tasks) {
			if (t.getAction().getName().equalsIgnoreCase(name))
				return t;
		}
		return null;
	}

	// Qui è definito la dimensione massima che la memoria raggiungerà
	private void addConnectionToMemory() {
		Action codomain = Action.setSendReadAction(new DataParameter(getSortBool()), new DataParameter(getSortData()));
		for (PartecipantProcess partecipant : collectPartecipants()) {
			Set<String> sendedtomemory = new HashSet<String>();
			List<String> childspartecipant = mCRL2.childTaskProcess(partecipant.getProcess(), mcrl2,
					new ArrayList<String>());
			for (String s : childspartecipant) {
				TaskProcess t = identiyTaskProcessName(s);
				for (DataParameter d : t.getAction().getParameters()) {
					Action output = new Action(partecipant.getActionToMemory(),
							new DataParameter("false", getSortBool()), d);
					t.addOutputAction(output);
					String myData ="";
					for(Entry<String, DataParameter> entry :dataplaceholder.entrySet()) {
						if(entry.getKey().equals(d.getName()) ||entry.getValue().getPlaeholder().equals(d.getName())) {
							myData = entry.getKey();
							break;
						}
					}
					
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
			Process lastsend = new Process(new Action(p.getActionToMemory(), new DataParameter("true", getSortBool()),
					new DataParameter(StructSort.empty, getSortData())));
			newpartecipant.addChild(lastsend.getName());
			newpartecipant.addInsideDef(lastsend);
			newpartecipant.addInsideDef(p.getProcess().getAllInsideDef().toArray(new Process[] {}));
			mcrl2.removePinInitSet(p.getName());
			p.setProcessPartecipant(newpartecipant);
			mcrl2.addInitSet(newpartecipant.getName());
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
