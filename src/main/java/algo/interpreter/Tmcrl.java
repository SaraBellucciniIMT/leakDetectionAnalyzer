package algo.interpreter;

import java.util.HashSet;
import java.util.Set;

import org.jbpt.algo.tree.tctree.TCType;
import org.jbpt.pm.IFlowNode;

import algo.TaskProcess;
import io.BPMNLabel;
import io.ExploitedRPST;
import io.ExtendedNode;
import spec.mcrl2obj.Process;

public class Tmcrl {

	private ExploitedRPST rpst;
	private ExtendedNode currentNode;
	private Process process;
	private Set<TaskProcess> taskprocesses;
	
	public Tmcrl(ExploitedRPST rpst) {
		this.rpst = rpst;
		taskprocesses = new HashSet<TaskProcess>();
		this.process = applyT(rpst.getRoot());
	}

	protected Process applyT(ExtendedNode node) {
		this.currentNode = node;
		if (node.getType().equals(TCType.TRIVIAL)) {
			if (node.getTag().equals(BPMNLabel.STARTEVENT))
				return new StartEvent().interpreter(this);
			else if (node.getTag().equals(BPMNLabel.ENDEVENT))
				return new EndEvent().interpreter(this);
			else if (node.getTag().equals(BPMNLabel.MESSAGE))
				return new CatchingEvent().interpreter(this);
			else {
				return new Task().interpreter(this);
			}
		} else if (node.getType().equals(TCType.POLYGON)) {
			return new Polygon(this.rpst.getOrderedDirectSuccessors(node)).interpreter(this);
		} else if (node.getType().equals(TCType.BOND)
				&& (node.getIRPTNodeAssociated().getEntry().getTag().equals(BPMNLabel.XOR)))
			return new XORBond(this.rpst.getDirectSuccessors(node)).interpreter(this);
		else if (node.getType().equals(TCType.BOND)
				&& (node.getIRPTNodeAssociated().getEntry().getTag().equals(BPMNLabel.AND)))
			return new ANDBond(this.rpst.getDirectSuccessors(node)).interpreter(this);
		else
			return null;

	}

	protected ExtendedNode getCurrentNode() {
		return this.currentNode;
	}
	
	public void addTaskProcess(TaskProcess tp) {
		this.taskprocesses.add(tp);
	}
	
	public Process getProcess() {
		return this.process;
	}
	
	public TaskProcess getProcessOfTask(IFlowNode flowNode){
		for(TaskProcess t : taskprocesses) {
			if(t.geExtendedNode().getName().equals(flowNode.getName()))
				return t;
		}
		return null;
	}

	@Override
	public String toString() {
		return "Tmcrl [process=" + process + "]";
	}

}
