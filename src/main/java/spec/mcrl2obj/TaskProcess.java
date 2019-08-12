package spec.mcrl2obj;

import java.util.HashSet;
import java.util.Set;

import io.ExtendedNode;

/*
 * It a process that represent a task in the bpmn model
 * Is composed by three elements:
 * - A set of input channels
 * - A storage set
 * - A set of output channels
 */
public class TaskProcess extends AbstractProcess {

	private ExtendedNode extendednode;
	// Because of sum e1...en : Data;
	private Set<Process> inputAction;
	private Set<Process> insidedef;
	private Set<Action> outputAction;
	private Action action;


	public TaskProcess(Action a, ExtendedNode en) {
		setName();
		this.action = a;
		this.extendednode = en;
		this.inputAction = new HashSet<Process>();
		this.outputAction = new HashSet<Action>();
		this.insidedef = new HashSet<Process>();
		// TODO Auto-generated constructor stub
	}

	// Controctor for buffer becuase they don't have a neither a correlated
	// extedendnode either an action
	public TaskProcess() {
		this.inputAction = new HashSet<Process>();
		this.insidedef = new HashSet<Process>();
		this.outputAction = new HashSet<Action>();
		this.action = null;
	}

	public ExtendedNode geExtendedNode() {
		return this.extendednode;
	}
	public void setBufferName() {
		setName();
	}
	public Action getAction() {
		return this.action;
	}

	public void addDataToAction(DataParameter d) {
		if(!this.action.containsParameter(d))
			this.action.addDataParameter(d);
	}

	public Set<Process> getInputAction() {
		return this.inputAction;
	}

	public Set<Action> getOutputAction() {
		return this.outputAction;
	}

	public void addInputAction(Process i, Process in1, Process in2) {
		this.inputAction.add(i);
		this.insidedef.add(in1);
		this.insidedef.add(in2);
	}

	public void addOutputAction(Action o) {
		this.outputAction.add(o);
	}

	public Object getTag() {
		return this.extendednode.getTag();
	}

	

	private Process getInsideDef(String name) {
		for (Process p : insidedef) {
			if (p.getName().equals(name))
				return p;
		}
		return null;
	}

	@Override
	public String toString() {
		
		String s =getName()+" = ";
		if (!inputAction.isEmpty()) {
			for (Process inputp : inputAction) {
				s = s + getInsideDef(inputp.getChildName(0)).toString() + ".";
				s = s + getInsideDef(inputp.getChildName(1)).toString() + ".";
			}
		}
		if (action != null) {
			s = s + action.toString();
			if (!outputAction.isEmpty())
				s = s + ".";
		}
		if (!outputAction.isEmpty()) {
			int i = 0;
			for (Action out : outputAction) {
				s = s + out.toString();
				if (i != (outputAction.size() - 1))
					s = s + ".";
				i++;
			}
		}
		return s;
	}

}
