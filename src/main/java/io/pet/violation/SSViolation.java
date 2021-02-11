package io.pet.violation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import com.google.common.collect.Sets;

import io.pet.PETLabel;
import io.pet.SecretSharing.SScomputation;
import io.pet.SecretSharing.SSsharing;
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
import spec.mcrl2obj.Processes.SUMProcess;
import spec.mcrl2obj.Processes.IfProcess;
import spec.mcrl2obj.Processes.ParticipantProcess;
import spec.mcrl2obj.Processes.TaskProcess;

/**
 * This is the SSViolation class, it defines functions in order to detect secret
 * sharing, additive secret sharing and function secret sharing violation
 * 
 * @author S. Belluccini
 *
 */
public class SSViolation extends AbstractViolation {

	private Placeholder id;
	private Placeholder m1;
	private Placeholder m2;
	// private Placeholder i;
	private Placeholder th;
	private static final String SSSV = "sssharingviolation";
	private static final String SSL = "sslist";
	//private static final String SSCV = "sscompviolation";
	private static final String SSC = "sscomp";
	private static SSViolation instance;

	private SSViolation() {
		Placeholder[] p = getPlaceholders(2, ISort.NAT);
		this.th = p[0];
		this.id = p[1];
		Placeholder[] m = getPlaceholders(2, Memory.nameSort());
		this.m1 = m[0];
		this.m2 = m[1];
	}

	public static SSViolation getInstance() {
		if (instance == null)
			instance = new SSViolation();

		return instance;
	}

	@Override
	protected String printMap() {
		String sssharing = "";
		if (Privacy.setPname.contains(PETLabel.SSSHARING)) {
			// sssharingviolation = Nat # Memory ->Bool
			String ssssharingviolation = MCRL2Utils.printMap(SSSV, ISort.BOOL, ISort.NAT, Memory.nameSort());
			String sslist = MCRL2Utils.printMap(SSL, Memory.nameSort(), ISort.NAT, Memory.nameSort(),
					Memory.nameSort());
			sssharing = ssssharingviolation + sslist;
		}
		String sscomputation = "";
		if (Privacy.setPname.contains(PETLabel.SSCOMPUTATION)) {
			//String sscf = MCRL2Utils.printMap(SSCV, ISort.BOOL, ISort.NAT, Memory.nameSort());
			String sscomp = MCRL2Utils.printMap(SSC, Memory.nameSort(), ISort.NAT, Memory.nameSort(),
					Memory.nameSort());
			sscomputation =  sscomp;
		}

		// String ssrecv = MCRL2Utils.printMap(SSRV, ISort.BOOL, Memory.nameSort());
		return sssharing + sscomputation;
	}

	// ((sslist(id,m1,0) >= TH)) -> sssharingviolation(id,m1) = true;
	/*
	 * String ssvfalse = MCRL2Utils.printifeqn( MCRL2Utils.printf(SSL,
	 * id.toString(), m.toString(), "0") + "<" + th.toString(),
	 * MCRL2Utils.printf(SSSV, id.toString(), th.toString(), m.toString()),
	 * ISort.FALSE.toString());
	 */
	/*
	 * String ssvtrue = MCRL2Utils.printifeqn( MCRL2Utils.printf(SSL, id.toString(),
	 * m.toString(), "0") + ">=" + th.toString(), MCRL2Utils.printf(SSSV,
	 * id.toString(), th.toString(), m.toString()), ISort.TRUE.toString());
	 */
	/*
	 * String sscvtrue = MCRL2Utils.printifeqn( MCRL2Utils.printf(SSC,
	 * id.toString(), m.toString(), "0") + ">=" + th.toString(),
	 * MCRL2Utils.printf(SSCV, id.toString(), th.toString(), m.toString()),
	 * ISort.TRUE.toString()); String sscvfalse = MCRL2Utils.printifeqn(
	 * MCRL2Utils.printf(SSC, id.toString(), m.toString(), "0") + "<" +
	 * th.toString(), MCRL2Utils.printf(SSCV, id.toString(), th.toString(),
	 * m.toString()), ISort.FALSE.toString());
	 */
	@Override
	protected String printEqn() {
		// is_pnode(head(m1)
		String isphead = MCRL2Utils.printIsPnH(m1.toString());
		// snd(pvalue(head(m1)))== id)
		String sndp = MCRL2Utils.printSndPvH(m1.toString());
		String sssharing = "";
		String ssvtrue = MCRL2Utils.printifeqn("#" + m1.toString() + ">=" + th.toString(),
				MCRL2Utils.printf(SSSV, th.toString(), m1.toString()), ISort.TRUE.toString());
		// (sslist(id,m1,0) < TH ) -> sssharingviolation(id,m1) = false;
		// (#m1 < th ) -> sssharingviolation(th,m1) = false;
		String ssvfalse = MCRL2Utils.printifeqn("#" + m1.toString() + "<" + th.toString(),
				MCRL2Utils.printf(SSSV, th.toString(), m1.toString()), ISort.FALSE.toString());
		if (Privacy.setPname.contains(PETLabel.SSSHARING)) {
			// (#m1 >= th) -> sssharingviolation(th,m1) = true;
						// is_sssharing(frt(pvalue(head(m1))))
			String isss = MCRL2Utils.printf(PETLabel.SSSHARING.is_value(),
					MCRL2Utils.printf(MCRL2Utils.frt, MCRL2Utils.printPvH(m1.toString())));
			// is_pnode(head(m1)) && is_sssharing(frt(pvalue(head(m1)))) &&
			// snd(pvalue(head(m1)))== id)
			String condAndss = MCRL2Utils.printAnd(isphead, isss, sndp + "==" + id.toString());
			// (is_pnode(head(m1)) && is_sssharing(frt(pvalue(head(m1)))) &&
			// snd(pvalue(head(m1)))== id) ->sslist(id,m1,i) = sslist(id,tail(m1),i+1);
			String ssltrue = MCRL2Utils.printifeqn(condAndss,
					MCRL2Utils.printf(SSL, id.toString(), m1.toString(), m2.toString()),
					MCRL2Utils.printf(SSL, id.toString(), MCRL2Utils.printf(MCRL2Utils.tail, m1.toString()),
							m2.toString() + "<|" + MCRL2Utils.printH(m1.toString())));
			// (is_pnode(head(m1))|| !is_sssharing(frt(pvalue(head(m1)))) ||
			// snd(pvalue(head(m1)))!= id)
			String condORss = MCRL2Utils.printOr("!" + isphead, "!" + isss, sndp + "!=" + id.toString());
			// (is_pnode(head(m1))|| !is_sssharing(frt(pvalue(head(m1)))) ||
			// snd(pvalue(head(m1)))!= id) ->sslist(id,m1,i) = sslist(id,tail(m1),i);
			String sslfalse = MCRL2Utils.printifeqn(condORss,
					MCRL2Utils.printf(SSL, id.toString(), m1.toString(), m2.toString()), MCRL2Utils.printf(SSL,
							id.toString(), MCRL2Utils.printf(MCRL2Utils.tail, m1.toString()), m2.toString()));
			// (m1 == []) -> sslist(id,m1,i) = i;
			String sslend = MCRL2Utils.printifeqn(m1.toString() + "== []",
					MCRL2Utils.printf(SSL, id.toString(), m1.toString(), m2.toString()), m2.toString());
			sssharing = ssvtrue + ssvfalse + ssltrue + sslfalse + sslend;
		}
		String sscomputation = "";
		if (Privacy.setPname.contains(PETLabel.SSCOMPUTATION)) {
			// (#m1 >= th) -> sscomputationviolation(th,m1) = true;
			/*String sscvtrue = MCRL2Utils.printifeqn("#" + m1.toString() + ">=" + th.toString(),
					MCRL2Utils.printf(SSCV, th.toString(), m1.toString()), ISort.TRUE.toString());*/
			// (sslist(id,m1,0) < TH ) -> sssharingviolation(id,m1) = false;
			// (#m1 < th ) -> sscomputationviolation(th,m1) = false;
			/*String sscvfalse = MCRL2Utils.printifeqn("#" + m1.toString() + "<" + th.toString(),
					MCRL2Utils.printf(SSCV, th.toString(), m1.toString()), ISort.FALSE.toString());*/
			String issc = MCRL2Utils.printf(PETLabel.SSCOMPUTATION.is_value(),
					MCRL2Utils.printf(MCRL2Utils.frt, MCRL2Utils.printPvH(m1.toString())));
			String condAndcc = MCRL2Utils.printAnd(isphead, issc, sndp + "==" + id.toString());
			String ssltruecc = MCRL2Utils.printifeqn(condAndcc,
					MCRL2Utils.printf(SSC, id.toString(), m1.toString(), m2.toString()),
					MCRL2Utils.printf(SSC, id.toString(), MCRL2Utils.printf(MCRL2Utils.tail, m1.toString()),
							m2.toString() + "<|" + MCRL2Utils.printH(m1.toString())));
			String condORcc = MCRL2Utils.printOr("!" + isphead, "!" + issc, sndp + "!=" + id.toString());
			String sslfalsecc = MCRL2Utils.printifeqn(condORcc,
					MCRL2Utils.printf(SSC, id.toString(), m1.toString(), m2.toString()), MCRL2Utils.printf(SSC,
							id.toString(), MCRL2Utils.printf(MCRL2Utils.tail, m1.toString()), m2.toString()));
			// (m1 == []) -> sslist(id,m1,i) = i;
			String sslendcc = MCRL2Utils.printifeqn(m1.toString() + "== []",
					MCRL2Utils.printf(SSC, id.toString(), m1.toString(), m2.toString()), m2.toString());
			sscomputation = /*sscvtrue + sscvfalse*/  ssltruecc + sslfalsecc + sslendcc;
		}
		return sssharing + sscomputation;
	}

	/**
	 * The secret sharing violation is added to the memory of those participants
	 * that aren't generating nor reconstructing the given secret. The checking is
	 * represented in a conjunction form as following:
	 * 
	 * sssharingviolation(id,th,union(m,e))|| sscompviolation(id,th,union(m,e))
	 * 
	 * memoryP2(m1:memory) = sum e:memory.tP2(e).
	 * (sssharingviolation(1,2,union(m1,e))|| sscompviolation(2,2,union(m1,e)))->
	 * VIOLATION.delta <> memoryP2(union(m1,e)) <>memoryP2(m1).delta;
	 * 
	 */
	@Override
	protected Triplet<Set<Action>, Set<CommunicationFunction>, Set<TaskProcess>> addChecking(MCRL2 mcrl2) {
		Set<Action> sssharing_action = new HashSet<Action>();
		Set<Pair<String, String>> sscomputatoininfo = new HashSet<Pair<String, String>>();

		Action recostruction = null;
		for (TaskProcess t : mcrl2.getTaskProcesses()) {
			if (t.hasPET()) {
				if (t.getPETLabel().equals(PETLabel.SSSHARING))
					sssharing_action.add(t.getAction());
				else if (t.getPETLabel().equals(PETLabel.SSRECONTRUCTION))
					recostruction = t.getAction();
				else if (t.getPETLabel().equals(PETLabel.SSCOMPUTATION)) {
					sscomputatoininfo.add(Pair.with(((SScomputation) t.getPET()).getGroupId(),
							((SScomputation) t.getPET()).getThreshold()));
				}
			}
		}

		Set<Action> newaction = new HashSet<Action>();
		Set<CommunicationFunction> newf = new HashSet<CommunicationFunction>();
		for (ParticipantProcess p : mcrl2.getParcipantProcesses()) {
			List<Pair<String, Process>> addToMem = new ArrayList<Pair<String, Process>>();
			Placeholder m = new Placeholder(Memory.nameSort());
			Placeholder e = new Placeholder(Memory.nameSort());
			String unionme = MCRL2Utils.printf(MCRL2Utils.unionf, m.toString(), e.toString());
			// If this party has no reconstruction task then he cannot know about the
			// reconstructed secret and the computation pieces needed
			if (!p.containActionTask(recostruction)) {
				// addToMem = MCRL2Utils.printf(SSRV, p.gete().toString());
				if (!sscomputatoininfo.isEmpty()) {
					for (Pair<String, String> pair : sscomputatoininfo) {
						// if (!addToMem.isEmpty())
						// addToMem += Operator.PARALLEL.getValue();
						// addToMem += MCRL2Utils.printf(SSCV, pair.getValue0(), pair.getValue1(),
						// unionme);
						String addToMemssc = MCRL2Utils.printf(SSC, pair.getValue0(), unionme, "[]");
						Process v = new Process(Operator.DOT,
								new Action(MCRL2.VIOLATION.getId(), new Placeholder(addToMemssc, Memory.nameSort())),
								MCRL2.DELTA);
						addToMem.add(Pair.with(MCRL2Utils.printf(SSSV, pair.getValue1(), addToMemssc), v));
					}

				}
				for (Action a : sssharing_action) {
					if (!p.containActionTask(a)) {
						/*
						 * if (!addToMem.isEmpty()) addToMem += Operator.PARALLEL.getValue(); addToMem
						 * += MCRL2Utils.printf(SSSV, String.valueOf(a.getPet().getIdPet()),
						 * ((SSsharing) a.getPet()).getTreshold(), unionme);
						 */
						String addToMemssc = MCRL2Utils.printf(SSL, String.valueOf(a.getPet().getIdPet()) , unionme,
								"[]");
						Process v = new Process(Operator.DOT,
								new Action(MCRL2.VIOLATION.getId(), new Placeholder(addToMemssc, Memory.nameSort())),
								MCRL2.DELTA);
						addToMem.add(Pair
								.with(MCRL2Utils.printf(SSSV,((SSsharing) a.getPet()).getTreshold(), addToMemssc), v));

					}
				}
			}
			if (!addToMem.isEmpty()) {
				Process memory = new Process(Operator.DOT);
				memory.addParameters(m);
				Action synMem = MCRL2.getTemporaryAction(e);
				SUMProcess sum = new SUMProcess(synMem);

				Action endAction = new Action(memory.getId(), memory.getId() + "(" + unionme + ")");
				IfProcess ifp = concatenateIfProcess(addToMem, endAction);
				memory.addChild(sum, ifp);
				p.setMemory(memory, synMem);
				Action synRes = MCRL2.getTemporaryAction(new Placeholder("", Memory.nameSort()));
				CommunicationFunction function = new CommunicationFunction(synRes, synMem, synMem);
				mcrl2.addCommunicaitonFunction(function);
				mcrl2.addAction(synRes, synMem, MCRL2.VIOLATION);
				mcrl2.addAllow(synRes, MCRL2.VIOLATION);
				mcrl2.addHide(synRes);

				newaction.add(synRes);
				newaction.add(synMem);
				newaction.add(MCRL2.VIOLATION);
				newf.add(function);
			}

		}
		return Triplet.with(newaction, newf, Sets.newHashSet());
	}



	@Override
	protected Placeholder[] getVar() {
		return new Placeholder[] { id, m1, m2, th };
	}

}
