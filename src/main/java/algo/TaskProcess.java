package algo;

import java.util.Set;

import io.ExtendedNode;
import spec.mcrl2obj.Action;

public class TaskProcess extends spec.mcrl2obj.Process {

	private ExtendedNode extendednode;
	private Set<Action> inputAction;
	private Set<Action> outputAction;
	
	public TaskProcess(Action a, ExtendedNode en) {
		super(a);
		this.extendednode = en;
		// TODO Auto-generated constructor stub
	}
	
	
	public void setExtendedNode(ExtendedNode en) {
		this.extendednode = en;
	}
	
	public ExtendedNode geExtendedNode() {
		return this.extendednode;
	}
	
	public void addInputAction(Action i) {
		this.inputAction.add(i);
	}
	
	public void addOutputAction(Action o) {
		this.outputAction.add(o);
	}
	
	public Object getTag() {
		return this.extendednode.getTag();
	}

}
