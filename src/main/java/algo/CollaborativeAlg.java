package algo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import spec.mcrl2obj.Action;
import spec.mcrl2obj.Buffer;
import spec.mcrl2obj.CommunicationFunction;
import spec.mcrl2obj.DataParameter;
import spec.mcrl2obj.Operator;
import spec.mcrl2obj.Process;
import spec.mcrl2obj.Sort;
import spec.mcrl2obj.TaskProcess;
import spec.mcrl2obj.mCRL2;

/*
 * 
 */
public class CollaborativeAlg extends AbstractTranslationAlg {

	private Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>> bpmn;
	private Set<Triple<IFlowNode, Set<DataNode>, IFlowNode>> internalCommList;
	private static final FlowNode epsilon = new Task();
	private final Sort sort = new Sort("Data");
	private Set<Pair<FlowNode, FlowNode>> messages;
	Set<Tmcrl> tmcrl2;
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
			Set<DataParameter> parameters = new HashSet<DataParameter>(size);
			triple.getMiddle()
					.forEach(dn -> parameters.add(new DataParameter(dataplaceholder.get(dn).getPlaeholder())));

			// Update the sorate of the right side Process
			if (triple.getLeft().equals(epsilon)) {
				TaskProcess R = getProcessOfTask(triple.getRight());
				triple.getMiddle().forEach(d -> {
					R.addDataToAction(new DataParameter(d.getName()));
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

			if (triple.getLeft().getTag().equals(BPMNLabel.TASK) && triple.getRight().getTag().equals(BPMNLabel.TASK)) {
				// Generate buffer and add the buffer to the init set
				generateCommunicationBuffer(i, o, parameters);

				Set<DataParameter> parametertosend = createOutputChannel(triple);
				Action send = Action.outputAction(parametertosend);
				S.addOutputAction(send);
				parameters.forEach(par -> R.addDataToAction(par));
				Action read = Action.inputAction(parameters);
				pread = new Process(read);

				this.mcrl2.addCommunicaitonFunction(createSendCommunication(send, i));
				this.mcrl2.addCommunicaitonFunction(createReadCommunication(o, read));

			} else if (triple.getLeft().getTag().equals(BPMNLabel.MESSAGE)) {
				if(!mcrl2.getInitSet().contains(S.getName()))
					mcrl2.addInitSet(S.getName());
				parameters.forEach(par -> R.addDataToAction(par));
				pread = new Process(i);
				S.addOutputAction(o);
				this.mcrl2.addCommunicaitonFunction(createReadCommunication(o, i));

			} else if (triple.getRight().getTag().equals(BPMNLabel.MESSAGE)) {
				if(!mcrl2.getInitSet().contains(R.getName()))
					mcrl2.addInitSet(R.getName());
				pread = new Process(i);
				Set<DataParameter> parametertosend = createOutputChannel(triple);
				Action send = Action.outputAction(parametertosend);
				S.addOutputAction(send);

				this.mcrl2.addCommunicaitonFunction(createSendCommunication(i, send));
			}
			ArrayList<String> childlist = new ArrayList<String>();
			childlist.add(suminput.getName());
			childlist.add(pread.getName());
			Process seqsuminput = new Process(Operator.DOT, childlist);
			R.addInputAction(seqsuminput, suminput, pread);

		}
		adjustSets();
	}

	private void adjustSets() {
		mcrl2.addAllow(Action.setSendAction());
		mcrl2.addAllow(Action.setReadAction());
		mcrl2.addHide(Action.setSendAction());
		mcrl2.addHide(Action.setReadAction());
	}

	private CommunicationFunction createSendCommunication(Action a, Action b) {
		List<Action> domainsend = new ArrayList<Action>();
		domainsend.add(a);
		domainsend.add(b);
		mcrl2.addAction(a);
		mcrl2.addAction(b);
		Action send = Action.setSendAction(a.getParameters());
		mcrl2.addAction(send);
		return new CommunicationFunction(domainsend, send);
	}

	private CommunicationFunction createReadCommunication(Action a, Action b) {
		List<Action> domainread = new ArrayList<Action>();
		domainread.add(a);
		domainread.add(b);
		mcrl2.addAction(a);
		mcrl2.addAction(b);
		Action read = Action.setReadAction(a.getParameters());
		mcrl2.addAction(read);
		return new CommunicationFunction(domainread, read);
	}

	private Set<DataParameter> createOutputChannel(Triple<IFlowNode, Set<DataNode>, IFlowNode> triple) {
		Set<DataParameter> parametertosend = new HashSet<DataParameter>();
		triple.getMiddle().forEach(d -> {
			if (isitFirst(triple.getLeft(), d)) {
				parametertosend.add(new DataParameter(d.getName()));
				sort.addType(d.getName());
			} else
				parametertosend.add(dataplaceholder.get(d));
		});
		return parametertosend;
	}

	// return the dataParameter established for that dataNode
	private Map<DataNode, DataParameter> dataplaceholder;

	// Gives a fixed placeholder to each dataparameter

	private void assignPlaceholder() {
		this.dataplaceholder = new HashMap<DataNode, DataParameter>();
		for (Triple<IFlowNode, Set<DataNode>, IFlowNode> triple : internalCommList) {
			triple.getMiddle().forEach(d -> {
				if (!dataplaceholder.containsKey(d)) {
					DataParameter dp = new DataParameter(d.getName());
					sort.addType(d.getName());
					dp.setPlaceHolder();
					dataplaceholder.put(d, dp);
				}
			});
		}
	}

	private boolean isitFirst(IFlowNode node, DataNode data) {
		for (Triple<IFlowNode, Set<DataNode>, IFlowNode> triple : internalCommList) {
			if (triple.getMiddle().contains(data) && triple.getRight().equals(node)
					&& !triple.getLeft().equals(epsilon)) {
				return false;
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
		System.out.println("there isn't a task process for node : " + f.toString());
		return null;
	}

	private void generateCommunicationBuffer(Action i, Action o, Set<DataParameter> parameters) {

		TaskProcess buffer = new TaskProcess();
		List<String> childlist = new ArrayList<String>();
		// generate i(e1,...,e_n)
		// Generate the sum : e1,...en:Data
		Process sum = new Process(Action.sumAction(parameters), Operator.SUM);
		childlist.add(sum.getName());
		Process input = new Process(i);
		childlist.add(input.getName());
		
		Process seqinputsum = new Process(Operator.DOT, childlist);
		buffer.addInputAction(seqinputsum, input, sum);
		buffer.addOutputAction(o);
		buffer.setBufferName();
		Process p = new Process(Action.emptyParameterAction(o.getName()));
		
		Buffer sumprocess = new Buffer(buffer, p);
		sumprocess.setBufferName();
		this.mcrl2.addProcess(sumprocess);
		this.mcrl2.addInitSet(buffer.getName());
	}

	// What is in output from a Intermediate Message Event is the data that was
	// sended
	private Set<DataNode> findData(IFlowNode f) {
		Set<DataNode> set = new HashSet<DataNode>();
		bpmn.forEach(b -> {
			for (DataNode d : b.getDataNodes()) {
				d.getWritingFlowNodes().forEach(flow -> {
					if (flow.getName().equals(f.getName()))
						set.add(d);
				});

			}
		});
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
				if (writingnodes.isEmpty() && !readnodes.isEmpty()) {
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

	@Override
	public mCRL2 getSpec() {
		tmcrl2 = new HashSet<Tmcrl>();
		for (Bpmn<BpmnControlFlow<FlowNode>, FlowNode> b : bpmn)
			tmcrl2.add(analyzeControlFlow(b));
		
		mcrl2 = new mCRL2();
		mcrl2.setSort(sort);
		tmcrl2.forEach(t -> {
			mcrl2.addProcesses(t.getProcess());
			mcrl2.addActions(t.getActions());
			mcrl2.addAllow(t.getActions());
			mcrl2.addInitSet(t.getFirstProcess());
		});
		analyzeData();

		return mcrl2;
	}

}
