package spec.mcrl2obj.Processes;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.javatuples.Pair;
import org.jbpt.pm.DataNode;

import com.google.common.collect.Sets;

import io.pet.AbstractDataPET;
import io.pet.AbstractTaskPET;
import io.pet.PETLabel;
import rpstTest.Utils;
import sort.Collection;
import sort.Data;
import sort.ISort;
import sort.Name;
import sort.Placeholder;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.Operator;
import spec.mcrl2obj.TaskAction;

/**
 * It is a process that represent a task in the bpmn model Is composed by three
 * elements: - A set of input channels - A storage set - A set of output
 * channels
 * 
 * @author S. Belluccini
 */
public class TaskProcess extends AbstractProcess {

	private Set<Process> inputAction;
	private Set<Action> outputAction;
	private AbstractProcess beforeTA;
	private AbstractProcess afterTA;
	private TaskAction action;
	private Action synAction;
	private IfProcess condition;

	/**
	 * Constructor for the TaskProcess a
	 * 
	 * @param a the action
	 */
	public TaskProcess(TaskAction a) {
		this.action = a;
		this.inputAction = new HashSet<Process>();
		this.outputAction = new HashSet<Action>();
	}

	/**
	 * Removes the addings to thi Task An adding can be: a codition, a process
	 * before the task or a process after the task
	 */
	public void removeAdding() {
		this.condition = null;
		this.beforeTA = null;
		this.afterTA = null;
	}

	/**
	 * It add a condition (i.e. the if then else ()-> <> operator of mclr2) to the
	 * process describing this task in the following way. T = sum d:Data.i(d). ...
	 * .Task.()-> <> .o()....
	 * 
	 * @param condition the clause to be checked
	 * @param iftrue    what happen if the condition is satisfied
	 * @param iffalse   what happen if the condition is not satisfied
	 */
	public void addCondition(String condition, AbstractProcess iftrue, AbstractProcess iffalse) {
		this.condition = new IfProcess(condition, iftrue, iffalse);
	}

	/**
	 * Adds a process before the current task action. Given P the process that we
	 * want to add it does T = T = sum d:Data.i(d). ... .P.Task.o()....
	 * 
	 * @param p the abstract process to be added
	 */
	public void addProcessBeforeTaskAction(AbstractProcess p) {
		this.beforeTA = p;
	}

	/**
	 * Adds a process after the current task action. Given P the process that we
	 * want to add it does T = T = sum d:Data.i(d). ... .Task.P.o()....
	 * 
	 * @param p the abstract process to be added
	 */
	public void addProcessAfterTasAction(AbstractProcess p) {
		this.afterTA = p;
	}

	/**
	 * Returns the AbstractTaskPET associated to this task
	 * 
	 * @return the AbstractTaskPET associated to this task
	 */
	public AbstractTaskPET getPET() {
		return this.action.getPet();
	}

	/**
	 * Returns true if this task has stereotype associated, false otherwise
	 * 
	 * @return Returns true if this task has stereotype associated, false otherwise
	 */
	public boolean hasPET() {
		if (this.action.getPet() != null)
			return true;
		else
			return false;
	}

	/**
	 * Returns the PETLabel associated to the this task, that depends from its
	 * action task
	 * 
	 * @return the PETLabel associated to the this task, that depends from its
	 *         action task
	 */
	public PETLabel getPETLabel() {
		if (hasPET())
			return this.action.getPet().getPETLabel();
		else
			System.err.println("There ISN'T a PET stereotype associated to this task");
		return null;
	}

	/**
	 * Checks if one of the input action receive the d data as input
	 * 
	 * @param d the data to check
	 * @return true if one the input action receive the d data as input, false
	 *         otherwise
	 */
	public boolean containsInputData(Data d) {
		if (getInputData().contains(d))
			return true;

		return false;
	}

	/**
	 * Computes an intersection between the set of the input action data and the
	 * given set
	 * 
	 * @param dataSort the set that we want to intersect
	 * @return an intersection between the set of the input action data and the
	 *         given set
	 */
	public Set<ISort> intersectWithInputDataSet(Set<ISort> dataSort) {
		Set<ISort> intersection = new HashSet<ISort>();
		for (ISort d : getInputData()) {
			if (dataSort.contains(d))
				intersection.add(d);
		}
		return intersection;
	}

	/**
	 * Returns the set of input processes in this task process
	 * 
	 * @return the set of input processes in this task process
	 */
	public Set<Process> getInputAction() {
		return this.inputAction;
	}

	/**
	 * Returns the set of output actions o this task process
	 * 
	 * @return the set of output actions o this task process
	 */
	public Set<Action> getOutputAction() {
		return this.outputAction;
	}

	/**
	 * Returns the set of data of the input actions of this task process
	 * 
	 * @return the set of data of the input actions of this task process
	 */
	public Set<ISort> getInputData() {
		Set<ISort> dataSort = new HashSet<ISort>();
		for (Process i : inputAction) {
			dataSort.addAll(Sets.newHashSet(i.getChildAtPosition(i.getSize() - 1).getParameters()));
		}
		return dataSort;
	}

	/**
	 * Returns the output data of this task process
	 * 
	 * @return the output data of this task process
	 */
	public Set<ISort> getOutputData() {
		Set<ISort> set = new HashSet<ISort>();
		for (Action o : outputAction) {
			set.addAll(Sets.newHashSet(o.getParameters()));
		}
		return set;
	}

	/**
	 * Sets the synchronization action with the party of this task in order to send
	 * its memory to the participant memory
	 * 
	 * @param nameSynAction name of the syn action of the party
	 */
	public void setSynWithMemory(Action syncAction) {
		if (syncAction.getParameters()[0].getNameSort().equals(Collection.nameSort())) {
			this.synAction = new TaskAction(syncAction.getId(), this.action.getParameters());
		} else {
			this.synAction = new Action(syncAction.getId(),
					syncAction.getId() + "(["
							+ new Placeholder(Utils.organizeParameterAsString(this.action.getParameters()) + "])",
									syncAction.parameters[0].getNameSort()));
		}
	}

	/**
	 * It removes the action that synchronize the memory of this process with the
	 * memory of its participants process before the current task action. Given P
	 * the process that we want to add it does T = T = sum d:Data.i(d). ...
	 * .Task.mem().o()... => T = T = sum d:Data.i(d). ... .Task.o()...
	 */
	public void removeSynAction() {
		this.synAction = null;
	}

	/**
	 * Returns the action of this task process
	 * 
	 * @return the action of this task process
	 */
	public TaskAction getAction() {
		return this.action;
	}

	/**
	 * Returns the dimension of the memory of this action
	 * 
	 * @return the dimension of the memory of this action
	 */
	public int getDimActionMemory() {
		return this.action.size();
	}

	/**
	 * Adds data to the input action
	 * 
	 * @param d the data to be added
	 */
	public void addInputDataToAction(DataNode... petnodes) {
		for (DataNode d : petnodes) {
			ISort param = null;
			if (actionHasParameter(d.getName()) == null) {
				if (!(hasPET() && ((param = getPET().defineInputDataSort(d)) != null))
						&& (d.getTag() == null || (param = ((AbstractDataPET) d.getTag()).printData(d)) == null)) {
					this.action.addParameters(new Data(new Name(d.getName())));
				} else
					this.action.addParameters(param);
			}
		}
	}

	/**
	 * Adds placeholder to the action
	 * 
	 * @param placeholders
	 */
	public void addInputDataToAction(Placeholder... placeholders) {
		this.action.addParameters(placeholders);
	}

	/**
	 * Add data to the action of this task process
	 * 
	 * @param petnodes     the set of nodes that will keep thier identity
	 * @param placeholders the set of placehoder
	 * @return an array with that describing how the data have been enconded
	 */
	public ISort[] addOutputDataToAction(Set<DataNode> petnodes, Set<Placeholder> placeholders) {
		ISort[] param = new ISort[0];
		if (!petnodes.isEmpty()) {
			ISort s = null;
			for (DataNode p : petnodes) {
				if ((s = actionHasParameter(p.getName())) == null) {
					if (!(hasPET() && (s = getPET().defineOutputDataSort(p)) != null)
							&& (p.getTag() == null || (s = ((AbstractDataPET) p.getTag()).printData(p)) == null))
						s = new Data(new Name(p.getName()));
					this.action.addParameters(s);
				}
				param = ArrayUtils.add(param, s);

			}
		}

		Placeholder[] plh = new Placeholder[placeholders.size()];
		if (!placeholders.isEmpty()) {
			plh = placeholders.toArray(plh);
			this.action.addParameters(plh);
		}
		return ArrayUtils.addAll(param, plh);
	}

	/**
	 * Checks if the action task contains a parameter with that name
	 * 
	 * @param realName the real name of the parameter
	 * @return the parameter if exists, otherwise null
	 */
	private ISort actionHasParameter(String realName) {
		return this.action.contain(realName);
	}

	public void addOutputDataToAction(ISort... sorts) {
		this.action.addParameters(sorts);
	}

	/**
	 * Adds an input action generating also the sumprocess connected to it
	 * 
	 * @param inputAction
	 * @param placeholders
	 */
	public void addInputAction(Action inputAction) {
		SUMProcess sump = new SUMProcess(inputAction);
		this.inputAction.add(sump);
	}

	public void addOutputAction(Action o) {
		this.outputAction.add(o);
	}

	/**
	 * {@inheritDoc} It defines how a task process is printed in a mcrl2 file. It
	 * returns a pair s.t. one the left there is the process definition of this
	 * process and on the right the process it self.
	 * 
	 * Given T s.t. T = sum d:Data.i(d). ... .Task.o()... it returns: Pair<T,T = sum
	 * d:Data.i(d). ... .Task.o()...>
	 */
	@Override
	public Pair<String, Set<String>> toPrint() {
		return Pair.with(this.getId(), Sets.newHashSet(toString()));
	}

	/**
	 * It defines how a task process should be printed. That is: T = sum
	 * d:Data.i(d). ... .Task.o()...
	 */
	public String toString() {
		String s = this.getId() + " = ";
		if (!this.inputAction.isEmpty()) {
			for (Process p : this.inputAction)
				s += p.toString() + Operator.DOT.getValue();

		}
		if (this.beforeTA != null)
			s += this.beforeTA.toString() + Operator.DOT.getValue();
		s += this.action.toString();
		if (this.afterTA != null)
			s += Operator.DOT.getValue() + this.afterTA.toString();
		if (this.synAction != null) {
			s += Operator.DOT.getValue() + this.synAction.toString();
		}
		if (condition != null)
			s += Operator.DOT.getValue() + condition.toString();
		if (!this.outputAction.isEmpty()) {
			for (Action a : outputAction)
				s += Operator.DOT.getValue() + a.toString();

		}
		return s;
	}

}
