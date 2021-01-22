package io.pet.violation;

import java.util.Set;

import org.javatuples.Triplet;

import com.google.common.collect.Sets;

import io.pet.PETLabel;
import io.pet.SecretSharing.SSreconstruction;
import rpstTest.Utils;
import sort.ISort;
import sort.Memory;
import sort.Placeholder;
import sort.Privacy;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.CommunicationFunction;
import spec.mcrl2obj.MCRL2;
import spec.mcrl2obj.MCRL2Utils;
import spec.mcrl2obj.Operator;
import spec.mcrl2obj.Processes.Process;
import spec.mcrl2obj.Processes.TaskProcess;

/**
 * This is the Reconstruction class, it defines function in order to check if a
 * secret from any type of secret sharing is in the end reconstructed, i.e. if
 * the the task with the reconstruction stereotypes receive the exact numbers of
 * shares or computation shares that it needs
 * 
 * @author S. Belluccini
 *
 */
public class Reconstruction extends AbstractViolation {
	private static final String is_r = "is_recostructed";
	private static final String l2b = "list2bag";
	private Placeholder m = new Placeholder(Memory.nameSort());
	private Placeholder b = new Placeholder(ISort.BAG_NAT);
	private Placeholder n = new Placeholder(ISort.NAT);

	@Override
	protected String printMap() {
		String list2bag = MCRL2Utils.printMap(l2b, ISort.BAG_NAT, Memory.nameSort(), ISort.BAG_NAT);
		String isr = MCRL2Utils.printMap(is_r, ISort.BOOL, ISort.BAG_NAT, ISort.NAT);
		return list2bag + isr;
	}

	@Override
	protected String printEqn() {
		String rec="";
		if (Privacy.setPname.contains(PETLabel.SSSHARING)) {
			String or1 = MCRL2Utils.printFFrtPvH(PETLabel.SSSHARING.is_value(), m.toString());
			if (Privacy.setPname.contains(PETLabel.SSCOMPUTATION)) {
				or1 = MCRL2Utils.printOr(MCRL2Utils.printFFrtPvH(PETLabel.SSSHARING.is_value(), m.toString()),
						MCRL2Utils.printFFrtPvH(PETLabel.SSCOMPUTATION.is_value(), m.toString()));
			}
			String if1 = MCRL2Utils.printAnd(MCRL2Utils.printIsPnH(m.toString()),
					or1);
			String namef1 = MCRL2Utils.printf(l2b, m.toString(), b.toString());
			String resultf1 = MCRL2Utils.printf(l2b, MCRL2Utils.printT(m.toString()),
					b.toString() + " +{" + MCRL2Utils.printSndPvH(m.toString()) + ":1}");
			String eqn1 = MCRL2Utils.printifeqn(if1, namef1, resultf1);
			String if2 = MCRL2Utils.printOr("!" + MCRL2Utils.printIsPnH(m.toString()),
					"!" + MCRL2Utils.printFFrtPvH(PETLabel.SSSHARING.is_value(), m.toString()));
			if(Privacy.setPname.contains(PETLabel.SSCOMPUTATION))
				if2 =  MCRL2Utils.printOr("!" + MCRL2Utils.printIsPnH(m.toString()),
						"!" + MCRL2Utils.printFFrtPvH(PETLabel.SSSHARING.is_value(), m.toString()),
						"!" + MCRL2Utils.printFFrtPvH(PETLabel.SSCOMPUTATION.is_value(), m.toString()));
			String resultf2 = MCRL2Utils.printf(l2b, MCRL2Utils.printT(m.toString()), b.toString());
			String eqn2 = MCRL2Utils.printifeqn(if2, namef1, resultf2);
			String eqn3 = MCRL2Utils.printifeqn(m.toString() + "== []", namef1, b.toString());
			String eqn4 = MCRL2Utils.printifeqn(
					MCRL2Utils.exist + " " + n.toString() + ":" + ISort.NAT + ". " + n.toString() + " in " + b.toString()
							+ "&&" + MCRL2Utils.printf("count", n.toString(), b.toString()) + ">=" + SSViolation.th,
					MCRL2Utils.printf(is_r, b.toString(), SSViolation.th.toString()), ISort.TRUE.toString());
			String eqn5 = MCRL2Utils.printifeqn(
					"!(" + MCRL2Utils.exist + " " + n.toString() + ":" + ISort.NAT + ". " + n.toString() + " in "
							+ b.toString() + "&&" + MCRL2Utils.printf("count", n.toString(), b.toString()) + ">="
							+ SSViolation.th + ")",
					MCRL2Utils.printf(is_r, b.toString(), SSViolation.th.toString()), ISort.FALSE.toString());
			rec = eqn1 +  eqn2 + eqn3 + eqn4 + eqn5;
		}
		return rec;
	}

	@Override
	protected Placeholder[] getVar() {
		return new Placeholder[] { m, b, SSViolation.th };
	}

	/**
	 * The reconstruction checking is added to that task that has the stereotype
	 * "SSRECOSTRUCTION"/"AddSSRECOSTRUCTION"/"FunSSRECOSTRUCTION"
	 * 
	 * If it is reconstructed the action NOVIOLATION is executed, otherwise the
	 * action
	 */
	@Override
	protected Triplet<Set<Action>, Set<CommunicationFunction>, Set<TaskProcess>> addChecking(MCRL2 mcrl) {
		for (TaskProcess t : mcrl.getTaskProcesses()) {
			if (t.hasPET() && t.getPETLabel().equals(PETLabel.SSRECONTRUCTION)) {
				String condition = MCRL2Utils.printf(is_r,
						MCRL2Utils.printf(l2b,
								"[" + Utils.organizeParameterAsString(t.getAction().getParameters()) + "]", "{0:0}"),
						((SSreconstruction) t.getPET()).getThreshold());
				t.addCondition(condition, new Process(Operator.DOT, MCRL2.NOVIOLATION, MCRL2.DELTA),
						new Process(Operator.DOT, MCRL2.VIOLATION, MCRL2.DELTA));
				mcrl.addAction(MCRL2.VIOLATION, MCRL2.NOVIOLATION);
				mcrl.addAllow(MCRL2.VIOLATION, MCRL2.NOVIOLATION);
				return Triplet.with(Sets.newHashSet(MCRL2.VIOLATION, MCRL2.NOVIOLATION), Sets.newHashSet(),
						Sets.newHashSet(t));
			}
		}
		return Triplet.with(Sets.newHashSet(), Sets.newHashSet(), Sets.newHashSet());
	}

}
