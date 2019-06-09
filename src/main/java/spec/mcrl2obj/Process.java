package spec.mcrl2obj;

import java.util.ArrayList;
import java.util.List;

import javax.management.openmbean.OpenMBeanOperationInfo;



public class Process extends AbstractProcess{

	// Process Name


	private Action action;
	private Operator op;
	private List<String> child;
	//private Process[] processes;
	/*
	 * Basic definition of process P = a;
	 */
	public Process(Action a) {
		this.action = a;
		this.child = new ArrayList<String>();
		setName();
	}

	/*
	 * Recursive definition Example . P = a+b P= c||d
	 */
	public Process(Operator op, List<String> p) {
		this.action = null;
		this.op = op;
		this.child = p;
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
		if(child.isEmpty())
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
		else
			System.err.println("No action in this process");
		return action;
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
	
	//Rimuove i vecchi figli presenti e li sostituisce con quelli nuovi
	public void setChild(List<String> childs) {
		this.child = new ArrayList<>();
		if(!childs.isEmpty())
		this.child.addAll(childs);
	}
	public String getChildName(int i) {
		return this.child.get(i);
	}
	
	public int getLength() {
		if(!child.isEmpty())
			return this.child.size();
		else if(!action.isEmpty())
			return 1;
		else 
			return -1;
	}

	@Override
	public String toString() {
		String s = "";
		if(getName() != null && ( op == null || !op.equals(Operator.SUM)) && (action == null|| action.isTau()))
			 s = s+ getName() + " = ";
		if(op != null && op.equals(Operator.SUM))
			return s + Operator.SUM.getValue() + " "+ action.toString();
		else if(action != null)
			return s + action.toString();
		else {
			s= s + "(";
			for(int i=0; i<child.size()-1; i++)
				s = s+child.get(i) + op.getValue();
			return s + child.get(child.size()-1) + ")";
		}
	}

}
