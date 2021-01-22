package spec.mcrl2obj;

import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.javatuples.Pair;

import com.google.common.collect.Sets;

import io.pet.AbstractTaskPET;
import rpstTest.Utils;
import sort.ISort;
import spec.mcrl2obj.Processes.AbstractProcess;

public class Action extends AbstractProcess {
	private String realName = "";

	/**
	 * Id that correspond to the field in the .bpmn file Example:
	 * <bpmn2:task id="Task_07fz7yg" name="A">
	 */

	private static final String tau = "tau";
	private boolean istau = false;
	private AbstractTaskPET pet;

	/**
	 * Constructor for Action, that sets both realName and Id
	 * 
	 * @param realName of the action
	 * @param id       the name that is actually printed out in the specification
	 */
	public Action(String realName, String id) {
		this.realName = realName;
		this.id = id;
	}

	/**
	 * Another constructor for the Action class
	 * 
	 * @param realName the real name of the action
	 * @param id       the associated id
	 * @param pet      the pet associated to this action
	 */
	public Action(String realName, String id, AbstractTaskPET pet) {
		this.realName = realName;
		this.id = id;
		this.pet = pet;
	}

	/**
	 * Another constructor for the Action class. The realName is set both as
	 * realName and Id of this action and if there are dataparamters are added to
	 * this action
	 * 
	 * @param realName the name that is used both for realName and Id of this task
	 * @param dataParameters contained by this action
	 */
	public Action(String realName, ISort... dataParameters) {
		this.realName = realName;
		this.id = realName;
		if (dataParameters.length != 0)
			this.parameters = dataParameters;
	}

	/**
	 * Constructor for silent events i.e. tau actions
	 */
	public Action() {
		istau = true;
	}

	public AbstractTaskPET getPet() {
		return this.pet;
	}

	/**
	 * Returns the real name of this activity i.e the name in the bpmn model
	 * 
	 * @return the real name of this activity i.e the name in the bpmn model
	 */
	public String getRealName() {
		return this.realName;
	}

	@Override
	public void addParameters(ISort... parameters) {
		if (!istau) {
			this.parameters = ArrayUtils.addAll(this.parameters, parameters);
		} else
			System.err.println("You cannot add parameters to a TAU action");
	}

	/*
	 * public String toString() { if (istau) return tau; String s = ""; if (id !=
	 * null) { s = id; if (parameters.length != 0) s += "(" +
	 * Utils.organizeParameterAsString(parameters) + ")"; } return s; }
	 */

	public boolean isTau() {
		return istau;
	}

	/*
	 * public boolean containsParameter(ISort d) { for (int i = 0; i <
	 * parameters.length; i++) { if (parameters[i].equals(d)) return true; } return
	 * false; }
	 */

	/**
	 * Returns the type of an action given the sorts of its parameters, i.e. Data #
	 * ... # Data
	 * 
	 * @return the type of an action given the sorts of its parameters, i.e. Data #
	 *         ... # Data
	 */
	public String printActionType() {
		String s = "";
		for (int i = 0; i < parametersLenght(); i++) {
			s += parameters[i].getNameSort();
			if (i != parametersLenght() - 1)
				s += "#";
		}
		return s;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Action other = (Action) obj;
		if (istau != other.istau)
			return false;
		if (realName == null) {
			if (other.realName != null)
				return false;
		} else if (!realName.equals(other.realName))
			return false;
		if (id == null) {
			if (other.getId() != null)
				return false;
		} else if (!id.equals(other.getId()))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (parametersLenght() != other.parametersLenght())
			return false;
		return true;
	}

	@Override
	public Pair<String, Set<String>> toPrint() {
		return Pair.with(toString(), Sets.newHashSet());
	}

	public String toString() {
		if (istau)
			return tau;
		String s = "";
		if (id != null) {
			s = id;
			if (parameters.length != 0)
				s += "(" + Utils.organizeParameterAsString(parameters) + ")";
		}
		return s;
	}
}
