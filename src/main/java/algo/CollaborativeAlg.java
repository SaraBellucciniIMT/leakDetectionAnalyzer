package algo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Triple;
import org.jbpt.algo.tree.rpst.RPST;
import org.jbpt.hypergraph.abs.IVertex;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.DataNode;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.IFlowNode;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.bpmn.BpmnControlFlow;
import org.jbpt.pm.bpmn.BpmnMessageFlow;
import org.jbpt.pm.bpmn.Task;
import algo.interpreter.Tmcrl;
import io.BPMNLabel;
import io.ExploitedRPST;
import spec.mCRL2;
import spec.mcrl2obj.*;
import spec.mcrl2obj.Process;

public class CollaborativeAlg extends AbstractTranslationAlg {

	private Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>> bpmn;
	private Set<Triple<IFlowNode, Set<DataNode>, IFlowNode>> internalCommList;
	private static final FlowNode epsilon = new Task();
	private static final Sort sort = new Sort("Data");
	Set<Tmcrl> tmcrl2;
	mCRL2 mcrl2;

	/*
	 * Analyze Control Flow 1° Generate rpst of Bpmn input model 2° apply T
	 */

	public CollaborativeAlg(Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>> bpmn) {
		this.bpmn = bpmn;
	}

	/*
	 * (non-Javadoc)
	 * @see algo.AbstractTranslationAlg#analyzeData()
	 */
	protected void analyzeData() {
		generateInternalCommunicationlist();
		for (Triple<IFlowNode, Set<DataNode>, IFlowNode> triple : internalCommList) {
			int size = triple.getMiddle().size();
			Set<DataParameter> parameters = new HashSet<DataParameter>(size);
			while (size != 0) {
				parameters.add(new DataParameter(sort));
				size--;
			}
			//Generate i(e_1,...,e_n)  
			Action i = Action.inputAction(parameters);
			//Generate  o(e_1,...,e_n)
			Action o = Action.outputAction(parameters);
			//sum : e1,...en:Data
			Action sum = Action.sumAction(parameters);
			DataParameter.resetIndex();
			
			//Generate buffer
			Process buffer = generateCommunicationBuffer(i,o,sum);
			
			TaskProcess S = getProcessOfTask(triple.getLeft());
			TaskProcess R = getProcessOfTask(triple.getRight());
			
			Set<Action> actionsend = new HashSet<Action>();
			actionsend.add(i);
			actionsend.add(Action.outputAction(parameters));
		}

		// take into account message flow comm

	}
	
	private void generateCommunicationFunction(Set<Action> domain,Action codomain) {
		
	}
	

	private TaskProcess getProcessOfTask(IFlowNode f) {
		for(Tmcrl t : tmcrl2) {
			TaskProcess task;
			if ((task = t.getProcessOfTask(f))!= null)
				return task;
		}
		return null;
	}
	private Process generateCommunicationBuffer(Action i, Action o, Action sum) {
		// Generate the sequence among input and output action : i(e_1,...,e_n).o(e_1,...,e_n);
		Process seqp = new Process(Operator.DOT, new Process[] { new Process(i), new Process(o) });

		// Generate the sum : e1,...en:Data
		Process sump = new Process(Operator.SUM, new Process[] { });

		// sum : e1,....,e_n : Data.i(e_1,...,e_n).o(e_1,...,e_n)
		Process seqSeqpSump = new Process(Operator.DOT, new Process[] { seqp, sump });

		// sum : e1,....,e_n : Data.i(e_1,...,e_n).o(e_1,...,e_n) + o(eps)
		return new Process(Operator.PLUS,
				new Process[] { seqSeqpSump, new Process(Action.emptyParameterAction(o.getName(), sort)) });
	}

	private void generateExternalCommunicationList() {
		bpmn.forEach(b->{
			for(BpmnMessageFlow m : b.getMessageFlowEdges()) {
				
			}
		});
	}
	
	//Return the flownode that correspond to the input vertix in that bpmn model
	private FlowNode getFlowToVertix(Bpmn<BpmnControlFlow<FlowNode>, FlowNode> b,String name) {
		for(FlowNode f : b.getFlowNodes()) {
			if()
		}
		return null;
	}
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
