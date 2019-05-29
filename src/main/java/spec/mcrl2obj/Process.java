package spec.mcrl2obj;


public class Process {

	// Process Name
	
	private String name;
	private Action action;
	private Operator op;
	private Process[] processes;
	private static int id =0;
	private static final String fiexdName = "P";
	
	/*
	 * Basic definition of process P = a;
	 */
	public Process(Action a) {
		this.action = a;
		this.name = fiexdName + (id++);
	}

	/*
	 * Recursive definition Example . P = a+b P= c||d
	 */
	public Process(Operator op, Process[] p) {
		this.op = op;
		this.processes = p;
		this.name = fiexdName + (id++);
	}

	public boolean isActivity() {
		if (this.action.isEmpty())
			return false;
		else
			return true;
	}
	
	public boolean isProcess() {
		if (this.action.isEmpty())
			return true;
		else
			return false;
	}

	public Operator getOperator() {
		return this.op;
	}

	@Override
	public String toString() {
		if (this.name != null && this.action != null)
			return name + "[" + action + "]";
		else if (this.name != null && this.processes != null) {
			String s = name + "[";
			for (int i = 0; i < processes.length - 1; i++)
				s = s.concat(processes[i].toString()) + this.op.getValue();
			return s.concat(processes[processes.length-1].toString() + "]");
		} else if (this.name == null && this.action != null)
			return "[" + action + "]";
		else {
			String s = "";
			for (int i = 0; i < processes.length - 1; i++)
				s = s.concat(processes[i].toString()) + this.op.getValue();
			return s.concat(processes[processes.length-1].toString());
		}

	}
}
