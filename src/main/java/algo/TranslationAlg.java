package algo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Triple;
import org.jbpt.algo.tree.rpst.RPST;

import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.DataNode;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.IFlowNode;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.bpmn.BpmnControlFlow;
import org.jbpt.pm.bpmn.Task;
import algo.interpreter.Tmcrl;
import io.BPMNLabel;
import io.ExploitedRPST;
import spec.mCRL2;
import spec.mcrl2obj.*;
import spec.mcrl2obj.Process;

public class TranslationAlg implements ITranslationAlg {

	private Bpmn<BpmnControlFlow<FlowNode>, FlowNode> bpmn;
	private Set<Triple<IFlowNode, Set<DataNode>, IFlowNode>> communicationList;
	private static final FlowNode epsilon = new Task();
	private static final Sort sort = new Sort("Data");
	Tmcrl t;

	/*
	 * Analyze Control Flow 1° Generate rpst of Bpmn input model 2° apply T
	 */
	private void analyzeControlFlow() {
		t = new Tmcrl(new ExploitedRPST(new RPST<ControlFlow<FlowNode>, FlowNode>(bpmn)));
	}

	private void analyzeData() {
		generatecommunicationlist();
		for (Triple<IFlowNode, Set<DataNode>, IFlowNode> triple : communicationList) {
			Process buffer = generateCommunicationBuffer(triple.getMiddle().size());
			TaskProcess S = t.getProcessOfTask(triple.getLeft());
			TaskProcess R = t.getProcessOfTask(triple.getRight());
			if(S.getTag().equals(BPMNLabel.TASK) && R.getTag().equals(BPMNLabel.TASK)) {
				
			}
		}

	}

	private Process generateCommunicationBuffer(int size) {

		Set<DataParameter> parameters = new HashSet<DataParameter>(size);
		while (size != 0) {
			parameters.add(new DataParameter(sort));
			size--;
		}
		Action i = Action.inputAction(parameters);
		Action o = Action.outputAction(parameters);
		DataParameter.resetIndex();

		// Generate the sequence among input and output action
		Process seqp = new Process(Operator.DOT, new Process[] { new Process(i), new Process(o) });

		// Generate the sum : e1,...en:Data
		Process sump = new Process(Operator.SUM, new Process[] { new Process(Action.sumAction(parameters)) });

		// sum : e1,....,e_n : Data.i(e_1,...,e_n).o(e_1,...,e_n)
		Process seqSeqpSump = new Process(Operator.DOT, new Process[] { seqp, sump });

		// sum : e1,....,e_n : Data.i(e_1,...,e_n).o(e_1,...,e_n) + o(eps)
		return new Process(Operator.PLUS,
				new Process[] { seqSeqpSump, new Process(Action.emptyParameterAction(o.getName(), sort))});
	}

	private void generatecommunicationlist() {
		epsilon.setTag("epsilon");
		epsilon.setName("epsilon");
		this.communicationList = new HashSet<Triple<IFlowNode, Set<DataNode>, IFlowNode>>();
		for (DataNode n : bpmn.getDataNodes()) {
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
						communicationList.add(Triple.of(w, dataset, r));
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
						communicationList.add(Triple.of(epsilon, dataset, r));
					}
				});

			}
		}
	}

	/*
	 * Check if already exist a couple send-receive that send a different data w.r.t
	 * to the one that we are considering now
	 */
	private Triple<IFlowNode, Set<DataNode>, IFlowNode> existingWriteReadCouple(IFlowNode writer, IFlowNode reader) {
		for (Triple<IFlowNode, Set<DataNode>, IFlowNode> t : communicationList) {
			if (t.getLeft().equals(writer) && t.getRight().equals(reader))
				return t;
		}
		return null;
	}

	@Override
	public mCRL2 getSpec(Bpmn<BpmnControlFlow<FlowNode>, FlowNode> bpmn) {
		this.bpmn = bpmn;
		analyzeControlFlow();
		analyzeData();

		return null;
	}

}
