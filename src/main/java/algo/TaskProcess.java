package algo;

import java.util.HashSet;
import java.util.Set;

import io.ExtendedNode;
import spec.mcrl2obj.*;
import spec.mcrl2obj.Process;

/*
 * It a process that represent a task in the bpmn model
 * Is composed by three elements:
 * - A set of input channels
 * - A storage set
 * - A set of output channels
 */
public class TaskProcess extends spec.mcrl2obj.Process {

	private ExtendedNode extendednode;
	private Set<Process> inputAction;
	private Set<Action> outputAction;
	
	public TaskProcess(Action a, ExtendedNode en) {
		super(a);
		this.extendednode = en;
		this.inputAction = new HashSet<Process>();
		this.outputAction = new HashSet<Action>();
		// TODO Auto-generated constructor stub
	}
	
	
	public void setExtendedNode(ExtendedNode en) {
		this.extendednode = en;
	}
	
	public ExtendedNode geExtendedNode() {
		return this.extendednode;
	}
	
	public void addInputAction(Process i) {
		this.inputAction.add(i);
	}
	
	public void addOutputAction(Action o) {
		this.outputAction.add(o);
	}
	
	public Object getTag() {
		return this.extendednode.getTag();
	}

}
