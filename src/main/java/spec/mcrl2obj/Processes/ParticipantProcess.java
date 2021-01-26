
package spec.mcrl2obj.Processes;

import java.util.HashSet;
import java.util.Set;
import org.javatuples.Pair;
import com.google.common.collect.Sets;
import sort.Collection;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.TaskAction;

/**
 * This is the class that represents a Participant in the bpmnfile. Every
 * Participant is identified by: -id -name - personal memory - dimension of the
 * memory
 * 
 * @author S. Belluccini
 */
public class ParticipantProcess extends AbstractProcess {

	private String nameParty;
	private Process memory;
	private Action synMemory;
	private AbstractProcess p;

	/**
	 * Constructors for the participant process class
	 * 
	 * @param p         the process composing the participant
	 * @param idParty   the id of this party
	 * @param nameParty the name of this party
	 */
	public ParticipantProcess(AbstractProcess p, String idParty, String nameParty) {
		this.id = idParty;
		this.nameParty = nameParty;
		this.p =p;
	}

	/**
	 * Returns the set of TaskProcess in this Participant Process
	 * 
	 * @return the set of TaskProcess in this Participant Process
	 */
	public Set<TaskProcess> getTaskProcesses() {
		return setOfTaskProcesses(this.p);
	}

	private static Set<TaskProcess> setOfTaskProcesses(AbstractProcess process) {
		if (process.getClass().equals(TaskProcess.class))
			return Sets.newHashSet((TaskProcess) process);
		else if (process.getClass().equals(Process.class) || process.getClass().equals(LoopProcess.class)) {
			Set<TaskProcess> s = new HashSet<TaskProcess>();
			for (int i = 0; i < ((Process) process).getSize(); i++) {
				s.addAll(setOfTaskProcesses(((Process) process).getChildAtPosition(i)));
			}
			return s;
		}
		return Sets.newHashSet();
	}

	/**
	 * Sets the synchronization of every TaskProcess in the party with its memory,
	 * in order to send the data gained by each task process to its correspondent
	 * participant. Returns the name of the synchronization action
	 * 
	 * @return the name of the synchronization action
	 */
	private void setConnectionMemory() {
		for (TaskProcess t : setOfTaskProcesses(this.p)) {
			if (t.getDimActionMemory() != 0)
				t.setSynWithMemory(synMemory);
		}
	}

	/**
	 * Adds a synchronization action to each tasks that has data to send to the
	 * party
	 * 
	 * @param process to be analyzed
	 */
	/*
	 * private void setConnectionMemory(AbstractProcess process) { int dim; if
	 * (process.getClass().equals(TaskProcess.class) && (dim =((TaskProcess)
	 * process).getDimActionMemory())!= 0) { ((TaskProcess)
	 * process).setSynWithMemory(synMemory); maxdim += dim; } else if
	 * (process.getClass().equals(Process.class)) { for (int i = 0; i < ((Process)
	 * process).getSize(); i++) { setConnectionMemory(((Process)
	 * process).getChildAtPosition(i)); } } }
	 */

	public TaskAction[] getActionTask() {
		TaskAction[] actions = new TaskAction[getTaskProcesses().size()];
		int i = 0;
		for (TaskProcess t : getTaskProcesses()) {
			actions[i++] = t.getAction();
		}
		return actions;
	}

	/**
	 * Checks if this party has this action task among its processes
	 * 
	 * @param a the action
	 * @return true if this party has this action task among its processes, false
	 *         otherwise
	 */
	public boolean containActionTask(Action a) {
		for (TaskProcess t : setOfTaskProcesses(this.p)) {
			if (t.getAction().equals(a))
				return true;
		}
		return false;
	}

	public void setMemory(Process memory, Action synMem) {
		this.memory = memory;
		this.synMemory = synMem;
		this.setConnectionMemory();
	}

	/**
	 * Cancel the memory of this process, its synchronization action and it removes
	 * the synchronization to all the tasks of this participant
	 */
	public void removeMemory() {
		this.memory = null;
		this.synMemory = null;
		for (TaskProcess t : setOfTaskProcesses(this.p)) {
			if (t.getDimActionMemory() != 0)
				t.removeSynAction();
			t.removeAdding();
		}
	}
	
	/**
	 * Checks if an action with that id is part of this participant
	 * @param s the id of the action
	 * @return true if it is part of this participant, false otherwise
	 */
	public boolean containActionTask(String s) {
		for (TaskProcess t : setOfTaskProcesses(this.p)) {
			if (t.getAction().getId().equals(s))
				return true;
		}
		return false;
	}

	/**
	 * Check if the process contains the given action
	 * 
	 * @param process abstract process
	 * @param a       the action
	 * @return true if the process contains the given action, false otherwise
	 */
	/*
	 * private boolean containActionTask(AbstractProcess process, Action a) { if
	 * (process.getClass().equals(TaskProcess.class) && ((TaskProcess)
	 * process).getAction().equals(a)) return true; else if
	 * (process.getClass().equals(Process.class)) { for (int i = 0; i < ((Process)
	 * process).getSize(); i++) { if (containActionTask(((Process)
	 * process).getChildAtPosition(i), a)) return true; } } return false; }
	 */

	/**
	 * Returns the name of this participant
	 * 
	 * @return the name of this participant
	 */
	public String getName() {
		return this.nameParty;
	}

	/**
	 * Returns the name of the memory mem({})
	 * 
	 * @return the name of the memory mem({})
	 */
	/*
	 * public Action getMemoryAct() { return this.memoryAct; }
	 */

	/**
	 * Returns the m placeholder used in the memory process and action in the union
	 * function
	 * 
	 * @return the m placeholder used in the memory process and action in the union
	 *         function
	 */
	/*
	 * public Placeholder getm() { return this.m; }
	 */

	/**
	 * Returns the e placeholder used in the memory process and action in the union
	 * function
	 * 
	 * @return the e placeholder used in the memory process and action in the union
	 *         function
	 */
	/*
	 * public Placeholder gete() { return this.e; }
	 */

	/**
	 * Returns the c placeholder that represents the collection already known by the
	 * memory i.e. Memory(c:Collection)
	 * 
	 * @return the c placeholder that represents the collection already known by the
	 *         memory i.e. Memory(c:Collection)
	 */
	/*
	 * public Placeholder getinitc() { return this.initc; }
	 */

	/**
	 * Returns the c placeholder that represents the collection that the memory
	 * receive as input i.e sum c:Collection.t(c)
	 * 
	 * @return the c placeholder that represents the collection that the memory
	 *         receive as input i.e sum c:Collection.t(c)
	 */
	/*
	 * public Placeholder getrevc() { return this.revc; }
	 */

	/**
	 * Sets the process of the party
	 * 
	 * @param proc the process
	 */
	public void setProcess(AbstractProcess proc) {
		this.p = proc;
	}

	/**
	 * Returns the process in the party
	 * 
	 * @return the process in the party
	 */
	public AbstractProcess getProcess() {
		return this.p;
	}
	
	/**
	 * Returns the initialization of the call to the memory of this party, i.e.
	 * Memory([])
	 * 
	 * @return the initialization of the call to the memory of this party, i.e.
	 *         Memory([])
	 */
	public String initializeMemoryCallP() {
		if (this.memory != null) {
			if (this.synMemory.getParameters()[0].getNameSort().equals(Collection.nameSort()))
				return this.memory.getId() + "({})";
			else
				return this.memory.getId() + "([])";

		} else
			return "";
	}

	/*
	 * public void addConditionToMemory(String condition) {
	 * this.memory.removeChildAtPosition(this.memory.getSize()); AbstractProcess ifp
	 * = new IfProcess(condition, new Process(Operator.DOT,
	 * MCRL2.VIOLATION,MCRL2.DELTA), this.memoryCallP); AbstractProcess ififp = new
	 * IfProcess("!"+b.toString(), ifp, new Process(Operator.DOT,new
	 * Action(this.memory.getId(), this.m.toString()),MCRL2.DELTA));
	 * this.memory.addChild(ififp); }
	 */

	/**
	 * Construct the memory of this participant process as: M(c1:Collection) = sum
	 * c2:Collection.t(c2).(!contain(c1+c2,{data to be verified})) ->
	 * M(union(c1,c2)) <> isin.delta
	 */
	/*
	 * public void generateMemory() { this.initc = new
	 * Placeholder(Collection.nameSort()); this.revc = new
	 * Placeholder(Collection.nameSort()); AbstractProcess sumrevc = new
	 * SUMProcess(revc); // Bool var //this.b = new Placeholder(ISort.BOOL); // Sum
	 * over b //AbstractProcess sumb = new SUMProcess(b); // Memory process var
	 * //this.m = new Placeholder(Memory.nameSort()); // Memory data var //this.e =
	 * new Placeholder(Memory.nameSort()); // Sum over memory data var
	 * //AbstractProcess sume = new SUMProcess(e); // t action, i.e. t(c2) Action t
	 * = MCRL2.getTemporaryAction(revc); this.synMemory = t.getId(); this.memory =
	 * new Process(Operator.DOT); //M(c1:Collection)
	 * this.memory.addParameters(initc); //M(c1+c2) this.memoryCallP = new
	 * Action(this.memory.getId(), new Placeholder(initc + "+" + revc
	 * ,Collection.nameSort())); // this.memoryAct = new Action(MCRL2.getDataName(),
	 * m); Process delta = new Process(Operator.DOT, this.memoryAct, MCRL2.DELTA);
	 * AbstractProcess ifp = new IfProcess("!" + b.toString() + "",
	 * this.memoryCallP, delta); this.memory.addChild(sumb, sume, t, ifp); }
	 */

	/**
	 * Returns the abstract process and memory representing this participant
	 */
	@Override
	public Pair<String, Set<String>> toPrint() {
		Pair<String, Set<String>> print = p.toPrint();
		if (this.memory != null) {
			String mem = this.memory.getId() + "(" + this.memory.printParameters() + ") = " + this.memory.toString();
			print.getValue1().add(mem);
		}

		return print;
	}

	public String toString() {
		Pair<String, Set<String>> print = toPrint();
		String st = this.id + "=" + print.getValue0() + ";\n";
		for (String s : print.getValue1())
			st += s + ";\n";
		return st;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

}
