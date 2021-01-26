package algo.interpreter;

import org.jbpt.algo.tree.tctree.TCType;
import io.BPMNLabel;
import io.ExploitedRPST;
import io.ExtendedNode;
import spec.mcrl2obj.Processes.AbstractProcess;
import spec.mcrl2obj.Processes.ParticipantProcess;

/**
 * Applies the Translation Function over the RPST object of a BPMN participant.
 * It generate a mCRL2 specification of a single participant without data
 * objects.
 * 
 * TODO: add loops (while) identification
 * @author S. Belluccini
 *
 */
public class Tmcrl {

	private static ExploitedRPST RPST;

	public static ParticipantProcess computeTmcrl(ExploitedRPST rpst, String bpmnid, String bpmnname) {
		RPST = rpst;
		//actions = new HashSet<Action>();
		return new ParticipantProcess(applyT(rpst.getRoot()), bpmnid,bpmnname);
	}

	protected static AbstractProcess applyT(ExtendedNode node) {
		//this.currentNode = node;
		if (node.getType().equals(TCType.TRIVIAL)) {
			if (node.equalsDescription(BPMNLabel.STARTEVENT))
				return new StartEvent().interpreter(node);
			else if (node.equalsDescription(BPMNLabel.ENDEVENT))
				return new EndEvent().interpreter(node);
			else if (node.equalsDescription(BPMNLabel.INTERCATCHMESSAGE))
				return new CatchingEvent().interpreter(node);
			else {
				return new Task().interpreter(node);
			}
		} else if (node.getType().equals(TCType.POLYGON)) {
			return new Polygon(RPST.getOrderedDirectSuccessors(node)).interpreter(node);
		} else if (node.getType().equals(TCType.BOND) && (node.equalsDescription(BPMNLabel.XOR)) && !(RPST.isLoop(node)))
			return new XORBond(RPST.getDirectSuccessors(node)).interpreter(node);
		else if (node.getType().equals(TCType.BOND) && (node.equalsDescription(BPMNLabel.AND))&& !(RPST.isLoop(node)))
			return new ANDBond(RPST.getDirectSuccessors(node)).interpreter(node);
		else if(node.getType().equals(TCType.BOND) && node.equalsDescription(BPMNLabel.EVENTBASEDG)&& !(RPST.isLoop(node)))
			return new EVENTBASEDGBond(RPST.getDirectSuccessors(node)).interpreter(node);
		else if(RPST.isLoop(node))
			return new LoopBond().interpreter(RPST.theLoop(node));
		else
			return null;
	}

	/*protected ExtendedNode getCurrentNode() {
		return this.currentNode;
	}*/

	/*protected void addProcess(AbstractProcess p) {
		this.processes.add(p);
	}

	public void addAction(Action a) {
		this.actions.add(a);
	}*/

	/*public Set<AbstractProcess> getProcess() {
		return this.processes;
	}

	public Set<Action> getActions() {
		return this.actions;
	}

	public ParticipantProcess getFirstProcess() {
		return partecipantProcess;
	}*/

	/*public TaskProcess getProcessOfTask(IFlowNode flowNode) {
		Set<TaskProcess> taskprocesses = new HashSet<>();
		processes.forEach(p -> {
			if (p.getClass().equals(TaskProcess.class))
				taskprocesses.add((TaskProcess) p);
		});
		for (TaskProcess t : taskprocesses) {
			if (t.getId().equals(flowNode.getId()))
				return t;
		}
		return null;
	}*/

}
