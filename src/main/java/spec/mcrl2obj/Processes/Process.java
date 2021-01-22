package spec.mcrl2obj.Processes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.javatuples.Pair;

import spec.mcrl2obj.Action;
import spec.mcrl2obj.Operator;

public class Process extends AbstractProcess implements Iterable<AbstractProcess> {

	private Operator op;
	private List<AbstractProcess> child = new ArrayList<AbstractProcess>();

	/**
	 * Constructor for the Process class
	 * 
	 * @param op the Operator of this process
	 * @param childs Abstract Process composing this process
	 */
	public Process(Operator op, AbstractProcess... childs) {
		this.op = op;
		for (int i = 0; i < childs.length; i++)
			this.child.add(childs[i]);
	}

	/**
	 * Protected constructor for the process class
	 * 
	 * @param op the operator of this process
	 */
	protected Process(Operator op) {
		this.op = op;
	}

	/**
	 * Another protected constructor to define an empty process
	 */
	protected Process() {
	}
	
	/**
	 * Returns true if one of the child is equal to the given action
	 * @param a the action
	 * @return true if one of the child is equal to the given action, false otherwise
	 */
	public boolean contains(Action a) {
		for(int i=0; i<child.size(); i++) {
			if(child.get(i).getClass().equals(Action.class) && ((Action)child.get(i)).equals(a))
				return true;
		}
		return false;
	}

	/**
	 * Returns the operator of this process
	 * 
	 * @return the operator of this process
	 */
	public Operator getOperator() {
		return this.op;
	}

	public AbstractProcess getChildAtPosition(int i) {
		return this.child.get(i);
	}

	/**
	 * Adds an AbstractProcess as part of this process
	 * 
	 * @param child AbstactProcess to be added
	 */
	public void addChild(AbstractProcess... child) {
		for (AbstractProcess c : child) {
			this.child.add(c);
		}
	}

	/**
	 * Adds an abstract process at index i
	 * 
	 * @param i the index
	 * @param p the abstract process
	 */
	public void addChildAtPosition(int i, AbstractProcess p) {
		child.add(i, p);
	}

	/**
	 * Returns an abstracProcess equal to this one but without the child at index i
	 * 
	 * @param i the index
	 * @return an abstracProcess equal to this one but without the child at index i
	 */
	public Process removeChildAtPosition(int i) {
		child.remove(i);
		return new Process(op,child.toArray(new AbstractProcess[child.size()]));
	}

	/**
	 * Returns the number of AbstractProcesses inside this process
	 * 
	 * @return the number of AbstractProcesses inside this process
	 */
	public int getSize() {
		return this.child.size();
	}


	@Override
	public Iterator<AbstractProcess> iterator() {
		return this.child.iterator();
	}

	@Override
	public Pair<String, Set<String>> toPrint() {
		Set<String> taskprocesses = new HashSet<String>();
		String s = "";
		for (int i = 0; i < child.size(); i++) {
			// If one of the child is a Task
			if (child.getClass().equals(TaskProcess.class)) {
				Pair<String,Set<String>> printtask = child.get(i).toPrint();
				taskprocesses.addAll(printtask.getValue1());
				s += printtask.getValue0();
			} else {
				Pair<String,Set<String>> print = child.get(i).toPrint();
				taskprocesses.addAll(print.getValue1());
				s += print.getValue0();
				if (i != child.size() - 1)
					s += op.getValue();
			}
		}
		if(!s.isBlank())
			s= "(" + s +")";
		return Pair.with(s, taskprocesses);
	}
	
	public String toString() {
		Pair<String,Set<String>> print = toPrint();
		return print.getValue0();
	}

}
