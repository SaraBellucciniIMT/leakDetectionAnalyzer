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
	//private Set<Process> insidedef;
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
		if (!this.action.containsParameter(d))
			this.action.addDataParameter(d);
	}

	public void addInputAction(Process i, Process... in1) {
		this.inputAction.add(i);
		if (in1.length != 0) {
			for (int j = 0; j < in1.length; j++)
				this.insidedef.add(in1[j]);
		}

	}

	public void addOutputAction(Action o) {
		this.outputAction.add(o);
	}
	private String recochecking ="";
	public void setRecostructionChecking(String s) {
		this.recochecking = s;
	}
	public String toStringinputAction() {
		String s = "";
		if (!inputAction.isEmpty()) {
			for (Process inputp : inputAction) {
				for (int i = 0; i < inputp.getLength(); i++) {
					s = s + getInsideDef(inputp.getChildName(i)).toString() + ".";
				}
			}
		}
		return s;
	}

	public String toStringoutputAction() {
		String s = "";
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

	@Override
	public String toString() {
		String s = getName() +" = ";

		s = s + toStringinputAction();
		if (action != null) {
			s = s + action.toString();
			if(!recochecking.isEmpty())
				s = s + "."+ recochecking;
			if (!outputAction.isEmpty())
				s = s + ".";
		}
		s = s + toStringoutputAction();
		return s;

	}

}
