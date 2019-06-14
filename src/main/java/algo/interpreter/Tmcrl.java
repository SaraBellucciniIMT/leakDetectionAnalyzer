package algo.interpreter;

import java.util.HashSet;
import java.util.Set;

import org.jbpt.algo.tree.tctree.TCType;
import org.jbpt.pm.IFlowNode;

import io.BPMNLabel;
import io.ExploitedRPST;
import io.ExtendedNode;
import spec.mcrl2obj.AbstractProcess;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.TaskProcess;

public class Tmcrl {

	private ExploitedRPST rpst;
	private ExtendedNode currentNode;
	private Set<Action> actions;
	private Set<AbstractProcess> processes = new HashSet<AbstractProcess>();
	private String firstProcess;

	public Tmcrl(ExploitedRPST rpst, String bpmnname) {
		this.rpst = rpst;
		actions = new HashSet<Action>();
		AbstractProcess first = applyT(rpst.getRoot());
		first.setId(bpmnname);
		processes.add(first);
		firstProcess = first.getName();
	}

	protected AbstractProcess applyT(ExtendedNode node) {
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

	protected void addProcess(AbstractProcess p) {
		this.processes.add(p);
	}

	public void addAction(Action a) {
		this.actions.add(a);
	}

	public Set<AbstractProcess> getProcess() {
		return this.processes;
	}

	public Set<Action> getActions() {
		return this.actions;
	}

	public String getFirstProcess() {
		return firstProcess;
	}

	public TaskProcess getProcessOfTask(IFlowNode flowNode) {
		Set<TaskProcess> taskprocesses = new HashSet<>();
		processes.forEach(p -> {
			if (p.getClass().equals(TaskProcess.class))
				taskprocesses.add((TaskProcess) p);
		});
		for (TaskProcess t : taskprocesses) {
			if(t.geExtendedNode().getName().equals("")){
				if(t.geExtendedNode().getId().equals(flowNode.getId())) 
						return t;
				continue;
			}
			if (t.geExtendedNode().getName().equals(flowNode.getName()))
				return t;
		}
		return null;
	}

}
