package io.pet.violation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.jbpt.hypergraph.abs.GObject;

import io.pet.MPC;
import io.pet.PETLabel;
import sort.Data;
import sort.Placeholder;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.CommunicationFunction;
import spec.mcrl2obj.MCRL2;
import spec.mcrl2obj.Processes.TaskProcess;

/**
 * This is the ParallelViolation class. It defines maps, equations, variable and
 * functions in order to detect violation in task that should happen in
 * parallel. Currently used to detect missed parallelism in MPC Pet
 * 
 * @author S. Belluccini
 *
 */
public class ParallelViolation extends AbstractViolation {

	private static ParallelViolation instance;

	public static ParallelViolation getInstance() {
		if (instance == null)
			instance = new ParallelViolation();

		return instance;
	}

	/**
	 * {@inheritDoc} Not implemented for this class. It returns an empty string
	 */
	@Override
	protected String printMap() {
		return "";
	}

	/**
	 * {@inheritDoc} Not implemented for this class. It returns an empty string
	 */
	@Override
	protected String printEqn() {
		return "";
	}

	/**
	 * {@inheritDoc} Not implemented for this class, it returns an empty array of
	 * placeholders
	 */
	@Override
	protected Placeholder[] getVar() {
		return new Placeholder[0];
	}

	/**
	 * The checking is provided by adding an action before every MPC task. To the
	 * MPC tasks with the same groupid an action with the same name is added. Then a
	 * synchronization function is forced among the actions with the same name.
	 * Example. t.MPC(Task1) t.MPC(Task2)
	 * 
	 * allow({sync}, comm({t|t->sync}))
	 * 
	 * If a deadlock is detected means that exists a trace in which the two MPC
	 * cannot synchronize i.e. are not executed synchronously
	 */
	@Override
	protected Triplet<Set<Action>, Set<CommunicationFunction>, Set<TaskProcess>> addChecking(MCRL2 mcrl) {
		Map<String, Pair<Action, Integer>> group_syncact = new HashMap<String, Pair<Action, Integer>>();
		Set<TaskProcess> updatetask = new HashSet<TaskProcess>();
		Set<Action> newaction = new HashSet<Action>();
		Set<CommunicationFunction> newcf = new HashSet<CommunicationFunction>();
		for (TaskProcess t : mcrl.getTaskProcesses()) {
			if (t.hasPET() && t.getPETLabel().equals(PETLabel.MPC)) {
				MPC pet = (MPC) t.getPET();
				Action sync;
				if (!group_syncact.containsKey(pet.getGroupId())) {
					group_syncact.put(pet.getGroupId(), Pair.with(MCRL2.getTemporaryAction(), 1));
				} else {
					Pair<Action, Integer> pair = group_syncact.get(pet.getGroupId());
					group_syncact.put(pet.getGroupId(), Pair.with(pair.getValue0(), pair.getValue1() + 1));
				}
				sync = group_syncact.get(pet.getGroupId()).getValue0();
				t.addProcessBeforeTaskAction(sync);
				updatetask.add(t);
				newaction.add(sync);
			}
		}

		if (!group_syncact.isEmpty()) {
			for (Pair<Action, Integer> pair : group_syncact.values()) {
				Action r = MCRL2.getTemporaryAction();
				CommunicationFunction f = new CommunicationFunction(r, pair.getValue0(), pair.getValue1());
				mcrl.addAllow(r);
				newaction.add(r);
				newcf.add(f);
			}
			mcrl.addAction(newaction.toArray(new Action[newaction.size()]));
		}
		return Triplet.with(newaction, newcf, updatetask);
	}

}
