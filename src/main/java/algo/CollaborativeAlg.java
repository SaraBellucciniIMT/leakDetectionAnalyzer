package algo;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.crypto.Data;

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
import spec.mCRL2;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.CommunicationFunction;
import spec.mcrl2obj.DataParameter;
import spec.mcrl2obj.Operator;
import spec.mcrl2obj.Process;
import spec.mcrl2obj.Sort;

public class CollaborativeAlg extends AbstractTranslationAlg {

	private Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>> bpmn;
	private Set<Triple<IFlowNode, Set<DataNode>, IFlowNode>> internalCommList;
	private static final FlowNode epsilon = new Task();
	private static final Sort sort = new Sort("Data");
	private Set<Pair<FlowNode, FlowNode>> messages;
	Set<Tmcrl> tmcrl2;
	mCRL2 mcrl2;

	/*
	 * Analyze Control Flow 1° Generate rpst of Bpmn input model 2° apply T
	 */

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
		generateExternalCommunicationList();
		assignPlaceholder();

		for (Triple<IFlowNode, Set<DataNode>, IFlowNode> triple : internalCommList) {
			int size = triple.getMiddle().size();
			Set<DataParameter> parameters = new HashSet<DataParameter>(size);
			while (size != 0) {
				parameters.add(new DataParameter(sort));
				size--;
			}
			if (triple.getLeft().getTag().equals(BPMNLabel.TASK) && triple.getRight().getTag().equals(BPMNLabel.TASK)) {
				// Generate i(e_1,...,e_n)
				Action i = Action.inputAction(parameters);
				// Generate o(e_1,...,e_n)
				Action o = Action.outputAction(parameters);
				// sum : e1,...en:Data
				Action sum = Action.sumAction(parameters);
				DataParameter.resetIndex();
				// Generate buffer
				Process buffer = generateCommunicationBuffer(i, o, sum);
				TaskProcess S = getProcessOfTask(triple.getLeft());
				TaskProcess R = getProcessOfTask(triple.getRight());
				//Create output channel for S
				Set<DataParameter> parametertosend = new HashSet<DataParameter>();
				triple.getMiddle().forEach(d->{
					if(isitFirst(triple.getLeft(), d))
						parametertosend.add(new DataParameter(d.getName(), sort));
					else
						parametertosend.add(new DataParameter(sort));
				});
				Action send = Action.outputAction(parametertosend);
				S.addOutputAction(send);
				Process suminput = new Process(Operator.SUM, new Process[] {new Process(Action.sumAction(parameters))});
				Action read =Action.inputAction(parameters);
				Process seqsuminput = new Process(Operator.DOT, new Process[] {suminput, new Process(read)});
				R.addInputAction(seqsuminput);
				Set<Action> domainsend = new HashSet<Action>();
				domainsend.add(send);
				domainsend.add(i);
				CommunicationFunction functionsend = new CommunicationFunction(domainsend, Action.setSendAction());
				Set<Action> domainread = new HashSet<Action>();
				domainsend.add(read);
				domainsend.add(o);
				CommunicationFunction functionread = new CommunicationFunction(domainsend, Action.setReadAction());
			}else
				continue;
		}

		// take into account message flow comm

	}



	// return the dataParameter established for that dataNode
	private Map<DataNode, DataParameter> datanodeparameter;

	// Gives a fixed placeholder to each dataparameter
	private void assignPlaceholder() {
		this.datanodeparameter = new HashMap<DataNode, DataParameter>();
		for (Triple<IFlowNode, Set<DataNode>, IFlowNode> triple : internalCommList) {
			triple.getMiddle().forEach(d -> {
				if (!datanodeparameter.containsKey(d)) {
					DataParameter dp = new DataParameter(d.getName(), sort);
					dp.setPlaceHolder();
					datanodeparameter.put(d, dp);
				}
			});
		}
	}

	private boolean isitFirst(IFlowNode node, DataNode data) {
		for (Triple<IFlowNode, Set<DataNode>, IFlowNode> triple : internalCommList) {
			if (triple.getMiddle().contains(data) && triple.getRight().equals(node)) {
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
		return null;
	}

	private Process generateCommunicationBuffer(Action i, Action o, Action sum) {
		// Generate the sequence among input and output action :
		// i(e_1,...,e_n).o(e_1,...,e_n);
		Process seqp = new Process(Operator.DOT, new Process[] { new Process(i), new Process(o) });

		// Generate the sum : e1,...en:Data
		Process sump = new Process(Operator.SUM, new Process[] {});

		// sum : e1,....,e_n : Data.i(e_1,...,e_n).o(e_1,...,e_n)
		Process seqSeqpSump = new Process(Operator.DOT, new Process[] { seqp, sump });

		// sum : e1,....,e_n : Data.i(e_1,...,e_n).o(e_1,...,e_n) + o(eps)
		return new Process(Operator.PLUS,
				new Process[] { seqSeqpSump, new Process(Action.emptyParameterAction(o.getName(), sort)) });
	}

	private void generateExternalCommunicationList() {
		for (Pair<FlowNode, FlowNode> pair : messages) {
			this.internalCommList.add(Triple.of(pair.getLeft(), findData(pair.getRight()), pair.getRight()));
		}
	}

	// What is in output from a Intermediate Message Event is the data that was
	// sended
	private Set<DataNode> findData(IFlowNode f) {
		Set<DataNode> set = new HashSet<DataNode>();
		bpmn.forEach(b -> {
			for (DataNode d : b.getDataNodes()) {
				System.out.println(d.getWritingFlowNodes());
				System.out.println(d.getName());
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
			System.out.println(b.getFlowNodes().toString());
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

		analyzeData();

		return null;
	}

}
