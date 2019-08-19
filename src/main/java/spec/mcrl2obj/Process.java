package spec.mcrl2obj;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Process extends AbstractProcess {

	// Process Name

	private Action action;
	protected Operator op;
	protected List<String> child = new ArrayList<String>();
	// Definitio of processes s.t: P = t0 .
	private Set<Process> insidedef;

	// private Process[] processes;
	protected Process() {}
	/*
	 * Basic definition of process P = a;
	 */
	public Process(Action a) {
		this.action = a;
		setName();
	}

	/*
	 * Recursive definition Example . P = a+b P= c||d
	 */
	public Process(Operator op, String... childs) {
		this.action = null;
		this.op = op;
		this.child = new ArrayList<String>();
		for (int i = 0; i < childs.length; i++)
			this.child.add(childs[i]);
		insidedef = new HashSet<Process>();
		setName();
	}

	// Just for sum process
	public Process(Action a, Operator op) {
		if (!op.equals(Operator.SUM))
			System.err.println("Process not allowed");
		else
			this.op = op;
		this.action = a;
		setName();
	}

	public boolean hasChild() {
		if (child.isEmpty())
			return false;
		return true;
	}

	public boolean isActivity() {
		if (this.action == null)
			return false;
		else
			return true;
	}

	public Action getAction() {
		if (this.action != null)
			return action;
		return action;
	}

	public boolean isProcess() {
		if (this.action == null)
			return true;
		else
			return false;
	}

	public Operator getOperator() {
		return this.op;
	}

	public List<String> getChild(){
		return this.child;
	}
	public void addInsideDef(Process... p) {
		for (Process proc : p)
			this.insidedef.add(proc);
	}

	public void addChild(String... name) {
		for (int i = 0; i < name.length; i++)
			this.child.add(name[i]);
	}

	// Rimuove i vecchi figli presenti e li sostituisce con quelli nuovi
	public void setChild(List<String> childs) {
		this.child = new ArrayList<>();
		if (!childs.isEmpty())
			this.child.addAll(childs);
		// Is just a reference to another process then remove Operator
		if (this.child.size() < 2)
			this.op = null;
	}

	public void setOpertor(Operator op) {
		this.op = op;
	}

	public String getChildName(int i) {
		return this.child.get(i);
	}

	protected List<String> childs(){
		return this.child;
	}
	public Set<Process> getAllInsideDef() {
		return this.insidedef;
	}

	public Process getInsideDef(String name) {
		for (Process p : this.insidedef) {
			if (p.getName().equals(name))
				return p;
		}
		return null;
	}

	public int getLength() {
		if (!child.isEmpty())
			return this.child.size();
		else if (action != null)
			return 1;
		else
			return -1;
	}

	public Process inInsideDef(String name) {
		for (Process p : insidedef) {
			if (p.getName().equals(name))
				return p;
		}
		return null;
	}

	private String toStringIf() {
		String s = "";

		for (int i = 0; i < this.getLength(); i++) {
			if (i == 1)
				s = s + "->";
			else if (i == 2) {
				s = s + "<>";
				Process p = this.getInsideDef(getChildName(i));
				for(int j=0; j< p.getLength(); j++) {
					s = s+ p.getInsideDef(p.getChildName(j)).toString();
					if(j != p.getLength()-1)
						s = s + p.getOperator().getValue();
				}
				continue;
			}
				
			s = s + this.getInsideDef(getChildName(i)).toString();
		}

		return s;
	}

	@Override
	public String toString() {
		String s = "";
		if(op != null && op.equals(Operator.IF))
			return toStringIf();
		
		if (getName() != null && (op == null || !op.equals(Operator.SUM)) && (action == null || action.isTau()))
			s = s + getName() + " = ";

		if (op != null && op.equals(Operator.SUM))
			return s + Operator.SUM.getValue() + " " + action.toString();
		else if (action != null)
			return s + action.toString();
		else {
				s = s + "(";
			for (int i = 0; i < child.size(); i++) {
				Process p;
				if ((p = inInsideDef(child.get(i))) == null) {
					s = s + child.get(i);
				} else
					s = s + p.toString();
				if (i != child.size() - 1)
					s = s + op.getValue();
			}
				s = s + ")";
			return s;
		}
	}

}
