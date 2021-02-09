package io.pet.violation;

import java.util.Set;
import org.javatuples.Quintet;
import com.google.common.collect.Sets;
import io.pet.PETLabel;
import rpstTest.Utils;
import sort.Collection;
import sort.Data;
import sort.ISort;
import sort.Placeholder;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.CommunicationFunction;
import spec.mcrl2obj.MCRL2;
import spec.mcrl2obj.MCRL2Utils;
import spec.mcrl2obj.Operator;
import spec.mcrl2obj.Processes.IfProcess;
import spec.mcrl2obj.Processes.ParticipantProcess;
import spec.mcrl2obj.Processes.Process;
import spec.mcrl2obj.Processes.SUMProcess;
import spec.mcrl2obj.Processes.TaskProcess;

public class ViolationInterpreter {

	/**
	 * Used to add a detection violation, in particular the detected violation is
	 * added based on the PET that are found in the mcrl2 specification
	 * 
	 * @param mcrl2 the specification
	 * @return all the action, function, placehodelrs etc.. that are added to the
	 *         specification in order to provide this check.
	 */
	public static Quintet<Set<Action>, Set<CommunicationFunction>, Set<Placeholder>, Set<String>, Set<TaskProcess>> detectViolation(
			MCRL2 mcrl2) {
		Quintet<Set<Action>, Set<CommunicationFunction>, Set<Placeholder>, Set<String>, Set<TaskProcess>> quintet = Quintet
				.with(Sets.newHashSet(), Sets.newHashSet(), Sets.newHashSet(), Sets.newHashSet(), Sets.newHashSet());
		for (PETLabel label : mcrl2.getPetLabel()) {
			if (label.equals(PETLabel.SSSHARING) || label.equals(PETLabel.SSCOMPUTATION)
					|| label.equals(PETLabel.SSRECONTRUCTION))
				quintet.add(SSViolation.getInstance().interpreter(mcrl2));
			else if (label.equals(PETLabel.CIPHER) || label.equals(PETLabel.DECODINGKEY))
				quintet.add(EncryViolation.getInstance().interpreter(mcrl2));

		}
		return quintet;
	}

	/**
	 * Used to detected a specific participant (from its name i.e. nameparty) and
	 * set the map, equation and communication function to detect if it gets to know
	 * a specific set of data (i.e. data)
	 * 
	 * @param mcrl2     the mcrl2 specification to be analyzed
	 * @param nameparty the name of the participant to be checked
	 * @param data      the set of data to be checked
	 * @return all the action, function, placehodelrs etc.. that are added to the
	 *         specification in order to provide this check.
	 */
	public static Quintet<Set<Action>, Set<CommunicationFunction>, Set<Placeholder>, Set<String>, Set<TaskProcess>> detectParty(
			MCRL2 mcrl2, String nameparty, Data[] data) {
		ParticipantProcess party = mcrl2.getParticipant(nameparty);
		Placeholder initc = new Placeholder(Collection.nameSort());
		Placeholder revc = new Placeholder(Collection.nameSort());
		Action t = MCRL2.getTemporaryAction(revc);
		SUMProcess sumrevc = new SUMProcess(t);
		String union = initc.toString() + "+" + revc.toString();
		Process memory = new Process(Operator.DOT);
		memory.addParameters(initc);
		IfProcess ifp = new IfProcess(
				"!" + MCRL2Utils.printf(MCRL2.CONTAIN.toString(),
						union + ",{" + Utils.organizeParameterAsString(data) + "}"),
				new Action(memory.getId(), new Placeholder(union, Collection.nameSort())),
				new Process(Operator.DOT, MCRL2.CONTAIN, MCRL2.DELTA));
		memory.addChild(sumrevc, ifp);
		party.setMemory(memory, t);

		// Update actions
		Action fresult = MCRL2.getTemporaryAction(new Placeholder("", Collection.nameSort()));
		mcrl2.addAction(t, MCRL2.CONTAIN, fresult);
		CommunicationFunction f = new CommunicationFunction(fresult, t, t);
		mcrl2.addCommunicaitonFunction(f);
		mcrl2.addAllow(fresult, MCRL2.CONTAIN);
		mcrl2.addHide(fresult);
		// --------------
		String map = MCRL2Utils.printMap(MCRL2.CONTAIN.toString(), ISort.BOOL, Collection.nameSort(),
				Collection.nameSort());
		// Update map
		mcrl2.addMap(map);
		// Update var
		Placeholder c1 = new Placeholder(Collection.nameSort());
		Placeholder c2 = new Placeholder(Collection.nameSort());
		mcrl2.addVar(c1, c2);
		// Update eqn
		String eqn1 = MCRL2Utils.printifeqn("(" + c1.toString() + "*" + c2.toString() + ") == " + c2.toString(),
				MCRL2Utils.printf(MCRL2.CONTAIN.toString(), c1.toString(), c2.toString()), ISort.TRUE.toString());
		String eqn2 = MCRL2Utils.printifeqn("(" + c1.toString() + "*" + c2.toString() + ") != " + c2.toString(),
				MCRL2Utils.printf(MCRL2.CONTAIN.toString(), c1.toString(), c2.toString()), ISort.FALSE.toString());
		mcrl2.addEqn(eqn1, eqn2);
		Set<String> strings = Sets.newHashSet(map, eqn1, eqn2);
		return Quintet.with(Sets.newHashSet(t, MCRL2.CONTAIN, fresult), Sets.newHashSet(f), Sets.newHashSet(c1, c2),
				strings, Sets.newHashSet());
	}

	/**
	 * It retrieves the mcrl2 object to the previous state
	 * 
	 * @param quintet the set of elements to be canceled from the mcrl2 object
	 * @param mcrl2   the object to be modified
	 */
	public static void rollback(
			Quintet<Set<Action>, Set<CommunicationFunction>, Set<Placeholder>, Set<String>, Set<TaskProcess>> quintet,
			MCRL2 mcrl2) {
		mcrl2.removeActoin(quintet.getValue0());
		mcrl2.removeAllow(quintet.getValue0());
		mcrl2.removeHide(quintet.getValue0());
		mcrl2.removeCommunicationFunction(quintet.getValue1());
		mcrl2.removeVar(quintet.getValue2());
		mcrl2.removeMap(quintet.getValue3());
		mcrl2.removeEqn(quintet.getValue3());
		for (TaskProcess task : quintet.getValue4()) {
			task.removeAdding();
		}
		for (ParticipantProcess p : mcrl2.getParcipantProcesses())
			p.removeMemory();

	}

}
