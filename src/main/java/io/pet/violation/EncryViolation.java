package io.pet.violation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.javatuples.Triplet;

import com.google.common.collect.Sets;

import io.pet.PETLabel;
import io.pet.Encryption.Cipher;
import io.pet.Encryption.KDecrypt;
import io.pet.Encryption.KEncrypt;
import sort.Data;
import sort.ISort;
import sort.Memory;
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

public class EncryViolation extends AbstractViolation {

	private Placeholder m;
	private Placeholder c;
	private Placeholder k;
	private static String hasc = "hascipher";
	private static String hask = "haskey";
	private static String encviol = "encryptionviolation";
	private static EncryViolation instance;

	private EncryViolation() {
		Placeholder[] plist = getPlaceholders(2,ISort.NAT);
		this.c = plist[0];
		this.k =plist[1];
		this.m = getPlaceholders(1, Memory.nameSort())[0];
	}

	public static EncryViolation getInstance() {
		if (instance == null)
			instance = new EncryViolation();

		return instance;
	}

	@Override
	protected String printMap() {
		String hascipher = MCRL2Utils.printMap(hasc, ISort.BOOL, Memory.nameSort(), ISort.NAT);
		String haskey = MCRL2Utils.printMap(hask, ISort.BOOL, Memory.nameSort(), ISort.NAT);
		String env = MCRL2Utils.printMap(encviol, ISort.BOOL, Memory.nameSort(), ISort.NAT,ISort.NAT);
		return hascipher + haskey + env;
	}

	@Override
	protected String printEqn() {
		String hascf = MCRL2Utils.printf(hasc, m.toString(), c.toString());
		String haskf = MCRL2Utils.printf(hask, m.toString(), k.toString());
		String envf = MCRL2Utils.printf(encviol, m.toString(), c.toString(),k.toString());
		String candtrue = MCRL2Utils.printAnd(MCRL2Utils.printIsPnH(m.toString()),
				MCRL2Utils.printFFrtPvH(PETLabel.CIPHER.is_value(), m.toString()),
				MCRL2Utils.printSndPvH(m.toString()) + "==" + c.toString());
		String hasctrue = MCRL2Utils.printifeqn(candtrue, hascf, ISort.TRUE.toString());
		String corfalse = MCRL2Utils.printOr("!" + MCRL2Utils.printIsPnH(m.toString()),
				"!" + MCRL2Utils.printFFrtPvH(PETLabel.CIPHER.is_value(), m.toString()),
				MCRL2Utils.printSndPvH(m.toString()) + "!=" + c.toString());
		String hasctail = MCRL2Utils.printifeqn(corfalse, hascf,
				MCRL2Utils.printf(hasc, MCRL2Utils.printT(m.toString()), c.toString()));
		String hascfalse = MCRL2Utils.printifeqn(m.toString() + "== []", hascf, ISort.FALSE.toString());
		String kandtrue = MCRL2Utils.printAnd(MCRL2Utils.printIsPnH(m.toString()),
				MCRL2Utils.printFFrtPvH(PETLabel.DECODINGKEY.is_value(), m.toString()),
				MCRL2Utils.printSndPvH(m.toString()) + "==" + k.toString());
		String hasktrue = MCRL2Utils.printifeqn(kandtrue, haskf, ISort.TRUE.toString());
		String korfalse = MCRL2Utils.printOr("!" + MCRL2Utils.printIsPnH(m.toString()),
				"!" + MCRL2Utils.printFFrtPvH(PETLabel.DECODINGKEY.is_value(), m.toString()),
				MCRL2Utils.printSndPvH(m.toString()) + "!=" + k.toString());
		String hasktail = MCRL2Utils.printifeqn(korfalse, haskf,
				MCRL2Utils.printf(hask, MCRL2Utils.printT(m.toString()), k.toString()));
		String haskfalse = MCRL2Utils.printifeqn(m.toString() + "==[]", haskf, ISort.FALSE.toString());
		String envtrue = MCRL2Utils.printifeqn(MCRL2Utils.printAnd(hascf, haskf), envf, ISort.TRUE.toString());
		String envfalse = MCRL2Utils.printifeqn(MCRL2Utils.printOr("!" + hascf, "!" + haskf), envf,
				ISort.FALSE.toString());
		return hasctrue + hasctail + hascfalse + hasktrue + hasktail + haskfalse + envtrue + envfalse;
	}

	@Override
	protected Placeholder[] getVar() {
		return new Placeholder[] { m, c,k };
	}

	@Override
	protected Triplet<Set<Action>, Set<CommunicationFunction>, Set<TaskProcess>> addChecking(MCRL2 mcrl2) {
		Set<TaskProcess> task_decrypt = new HashSet<TaskProcess>();
		Set<TaskProcess> task_encrypt = new HashSet<TaskProcess>();
		for (TaskProcess t : mcrl2.getTaskProcesses()) {
			if (t.hasPET()) {
				if (t.getPETLabel().equals(PETLabel.KDECRYPT)) {
					task_decrypt.add(t);
				} else if (t.getPETLabel().equals(PETLabel.KENCRYPT))
					task_encrypt.add(t);
			}
		}
		Set<Action> actions = Sets.newHashSet();
		Set<CommunicationFunction> functions = Sets.newHashSet();
		Set<TaskProcess> tasks = Sets.newHashSet();
		for (ParticipantProcess p : mcrl2.getParcipantProcesses()) {
			Placeholder m = new Placeholder(Memory.nameSort());
			Placeholder e = new Placeholder(Memory.nameSort());
			String unionme = MCRL2Utils.printf(MCRL2Utils.unionf, m.toString(), e.toString());
			String addToMem = "";
			Set<String> cyphernottocheck = new HashSet<String>();
			if (!task_decrypt.isEmpty()) {
				for (TaskProcess decrypt : task_decrypt) {
					if (p.containActionTask(decrypt.getAction())) {
						for (Entry<String, String> entry : Cipher.idobj_idkey.entrySet()) {
							if (entry.getValue().equals(((KDecrypt) decrypt.getAction().getPet()).getGroupId()))
								cyphernottocheck.add(entry.getKey());
						}

					}
				}
			}
			for (TaskProcess encrypt : task_encrypt) {
				if (p.containActionTask(encrypt.getAction()))
					cyphernottocheck.add(((KEncrypt) encrypt.getAction().getPet()).getGroupId());
			}
			for (Entry<String, String> entry : Cipher.idobj_idkey.entrySet()) {
				if (!cyphernottocheck.contains(entry.getKey())) {
					if (!addToMem.isEmpty())
						addToMem += Operator.PARALLEL.getValue();
					addToMem += MCRL2Utils.printf(encviol, unionme, entry.getKey(), entry.getValue());
				}
			}

			if (!addToMem.isEmpty()) {
				Process memory = new Process(Operator.DOT);
				memory.addParameters(m);
				Action synMem = MCRL2.getTemporaryAction(e);
				SUMProcess sum = new SUMProcess(synMem);
				IfProcess ifp = new IfProcess(addToMem, new Process(Operator.DOT, MCRL2.VIOLATION, MCRL2.DELTA),
						new Action(memory.getId(), memory.getId() + "(" + unionme + ")"));
				memory.addChild(sum, ifp);
				p.setMemory(memory, synMem);
				Action synRes = MCRL2.getTemporaryAction(new Placeholder("", Memory.nameSort()));
				CommunicationFunction function = new CommunicationFunction(synRes, synMem, synMem);
				mcrl2.addCommunicaitonFunction(function);
				mcrl2.addAction(synRes, synMem, MCRL2.VIOLATION);
				mcrl2.addAllow(synRes, MCRL2.VIOLATION);
				mcrl2.addHide(synRes);
				actions.addAll(Sets.newHashSet(synRes, synMem, MCRL2.VIOLATION));
				functions.add(function);
			}
		}
		return Triplet.with(actions	,functions,tasks);
	}
	/*
	 * String groupid = ((KDecrypt) decrypt.getAction().getPet()).getGroupId(); for
	 * (TaskProcess encrypt : task_encrypt) { if (((KEncrypt)
	 * encrypt.getAction().getPet()).getGroupId().equals(groupid) &&
	 * !p.containActionTask(encrypt.getAction())) if (!addToMem.isEmpty()) addToMem
	 * += Operator.PARALLEL.getValue(); addToMem += MCRL2Utils.printf(encviol,
	 * unionme, groupid); }
	 */
}
