package io.pet.violation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.javatuples.Pair;
import org.javatuples.Quintet;
import org.javatuples.Triplet;

import com.google.common.collect.Sets;
import sort.Placeholder;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.CommunicationFunction;
import spec.mcrl2obj.MCRL2;
import spec.mcrl2obj.Processes.IfProcess;
import spec.mcrl2obj.Processes.Process;
import spec.mcrl2obj.Processes.TaskProcess;

public abstract class AbstractViolation implements IViolation {

	private Set<Placeholder> placeholders = new HashSet<Placeholder>();
	

	/**
	 * Adds the current violation to the mCRL2 specification
	 * 
	 * @param mcrl2 the mcrl2 specification
	 */
	public Quintet<Set<Action>,Set<CommunicationFunction>, Set<Placeholder>,Set<String>,Set<TaskProcess>> interpreter(MCRL2 mcrl2) {
		String map = this.printMap();
		String eqn = this.printEqn();
		Placeholder[] placeholder = this.getVar();
		mcrl2.addMap(map);
		mcrl2.addEqn(eqn);
		mcrl2.addVar(placeholder);
		Triplet<Set<Action>,Set<CommunicationFunction>,Set<TaskProcess>> pair = this.addChecking(mcrl2);
		return Quintet.with(pair.getValue0(), pair.getValue1(), Sets.newHashSet(placeholder), Sets.newHashSet(map,eqn), pair.getValue2());
		
	}

	/**
	 * Returns an array of placeholder among the one that already exists, and if
	 * they don't it creates new of them (to have commong var in the mCRL2
	 * specification when it is possible)
	 * 
	 * @param number the number of placeholders of that type i.e. sort
	 * @param nameSort the name of the sort
	 * @return  an array[number] of placeholder
	 */
	protected Placeholder[] getPlaceholders(int number, String nameSort) {
		int n = 0;
		Placeholder[] result = new Placeholder[number];
		for (Placeholder p : placeholders) {
			if (p.getNameSort().equals(nameSort)) {
				result[n] = p;
				n++;
			}
		}
		while (n < number) {
			result[n] = new Placeholder(nameSort);
			this.placeholders.add(result[n]);
			n++;

		}
		return result;
	}
	
	/**
	 * Returns the function (f_name : Dom # ... # Dom -> Cod) to be added in the map
	 * part of the mCRL2 specification
	 * 
	 * @return a string containing a list of maps with this structure : f_name : Dom
	 *         # ... # Dom -> Cod)
	 */
	protected abstract String printMap();

	/**
	 * Returns the equation ( f_name = result, cond -> f_name = result) to be added
	 * in the eqn part of the mCRL2 specification
	 * 
	 * @return a string containing a list of equations
	 */
	protected abstract String printEqn();

	/**
	 * Returns an array of placeholder used in the eqn to be added in the var part
	 * of the mCRL2 specification
	 * 
	 * @return an array of placeholder
	 */
	protected abstract Placeholder[] getVar();

	/**
	 * Returns a new mCRL2 object where the checking functions have been added to the corresponding process or action.
	 * @param mcrl MCRL2 object to which we add the checking
	 * @return a new mCRL2 object where the checking functions have been added to the corresponding process or action.
	 */
	protected abstract Triplet<Set<Action>,Set<CommunicationFunction>,Set<TaskProcess>> addChecking(MCRL2 mcrl);
	
	protected IfProcess concatenateIfProcess(List<Pair<String, Process>> list, Action endAction) {
		if (list.size() == 1) {
			return new IfProcess(list.get(0).getValue0(), list.get(0).getValue1(), endAction);
		} else
			return new IfProcess(list.get(0).getValue0(), list.get(0).getValue1(),
					concatenateIfProcess(list.subList(1, list.size()), endAction));

	}

}
