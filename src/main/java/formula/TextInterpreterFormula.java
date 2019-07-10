/**
 * 
 */
package formula;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.google.common.collect.Sets;

import io.pet.PET;
import io.pet.PETLabel;
import io.pet.SScomputation;
import io.pet.SSsharing;
import spec.mcrl2obj.AbstractProcess;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.PartecipantProcess;
import spec.mcrl2obj.TaskProcess;
import spec.mcrl2obj.mCRL2;

/**
 * @author sara INPUT: string from terminal i.e string from the user OUTPUT :
 *         FORMULA
 */
public abstract class TextInterpreterFormula {

	protected static final String openpossibilityformula = "<true*.";
	protected static final String closepossibilityformula = ">true";
	private static final String fileName = "formula";
	private static int id = 0;
	public static final String violation = "ssviolation";
	private static String parameter = "p";

	public static String toFile(mCRL2 mcrl2, String path , String idname, Set<String> data, String tag) {
		File file = new File(path + idname + fileName + ".mcf");
		while (file.exists())
			file = new File(path +idname + fileName + (id++) + ".mcf");

		try (BufferedWriter output = new BufferedWriter(new FileWriter(file))) {

			if (identifyIdTaskFormula(mcrl2, idname) != null)
				output.write(TaskFormula.generateTaskFormula(mcrl2, idname, data));
			else if (tag.equals(violation))
				output.write(sssharingChecking(mcrl2));
			else
				output.write(PartecipantFormula.generatePartecipantFormula(mcrl2, idname, data));

			output.close();
			System.out.println(file.getName() + " GENERATED");
		} catch (Exception e) {
			System.err.println("error in generating " + idname + fileName);
		}

		return file.getName();
	}

	protected static String sssharingChecking(mCRL2 mcrl2) {
		Map<PET, Set<String>> map = mcrl2.getSensibleData();
		int treshold = 0;
		Map<Integer, Set<String>> groupcomputation = new HashMap<Integer, Set<String>>();
		Set<String> recontruction = new HashSet<String>();
		for (Entry<PET, Set<String>> entry : map.entrySet()) {
			if (entry.getKey().getPET().equals(PETLabel.SSSHARING)) {
				treshold = ((SSsharing) entry.getKey()).getTreshold();
			} else if (entry.getKey().getPET().equals(PETLabel.SSCOMPUTATION)) {
				if (groupcomputation.containsKey(((SScomputation) entry.getKey()).getGroupId())) {
					groupcomputation.get(((SScomputation) entry.getKey()).getGroupId()).addAll(entry.getValue());
				} else {
					groupcomputation.put(((SScomputation) entry.getKey()).getGroupId(), entry.getValue());
				}
			} else if (entry.getKey().getPET().equals(PETLabel.SSRECONTRUCTION))
				recontruction.addAll(entry.getValue());
		}

		String formula = "";
		Set<PartecipantProcess> partecipantProcesses = mcrl2.getParcipantProcesses();
		Set<PartecipantProcess> remove = new HashSet<PartecipantProcess>();
		for (PartecipantProcess partecipant : partecipantProcesses) {
			if (reconstructionPartecipant(mcrl2, partecipant.getId()))
				remove.add(partecipant);
		}
		partecipantProcesses.removeAll(remove);

		for (Entry<Integer, Set<String>> entry : groupcomputation.entrySet()) {
			Set<Set<String>> list = Sets.powerSet(entry.getValue());
			for (Set<String> d : list) {
				String subformula = "";
				if (d.size() == treshold) {
					if (!formula.isEmpty())
						formula = formula + "||";
					int i = 0;
					for (PartecipantProcess partecipant : partecipantProcesses) {
						subformula = subformula
								+ PartecipantFormula.generatePartecipantFormula(mcrl2, partecipant.getId(), d);
						if (i != partecipantProcesses.size() - 1)
							subformula = subformula + "||";
						i++;
					}
				}
				formula = formula + subformula;
			}
		}
		// Because for the moment I don't know who the id reconstruction so if you have
		// more than one piece is not ok
		Set<Set<String>> listrec = Sets.powerSet(recontruction);

		for (Set<String> d : listrec) {
			String subformula = "";
			if (d.size() == treshold) {
				if (!formula.isEmpty())
					formula = formula + "||";
				int i = 0;
				for (PartecipantProcess partecipant : partecipantProcesses) {
					subformula = subformula
							+ PartecipantFormula.generatePartecipantFormula(mcrl2, partecipant.getId(), d);
					if (i != partecipantProcesses.size() - 1)
						subformula = subformula + "||";
					i++;
				}
			}
			formula = formula + subformula;
		}
		return formula;

	}

	protected static TaskProcess identifyTaskFormula(mCRL2 mcrl, String name) {
		for (AbstractProcess ab : mcrl.getProcesses()) {
			if (ab.getClass().equals(TaskProcess.class) && ((TaskProcess) ab).getAction().getName().equals(name))
				return (TaskProcess) ab;
		}
		return null;
	}

	public static TaskProcess identifyIdTaskFormula(mCRL2 mcrl, String idtask) {
		for (AbstractProcess ab : mcrl.getProcesses()) {
			if (ab.getClass().equals(TaskProcess.class) && ((TaskProcess) ab).getAction().getId().equals(idtask))
				return ((TaskProcess) ab);
		}
		return null;
	}

	protected static Set<String> generatePar(int n) {
		Set<String> s = new HashSet<String>();
		while (n != 0) {
			s.add(parameter + id++);
			n--;
		}
		return s;
	}

	private static boolean reconstructionPartecipant(mCRL2 mcrl, String name) {
		List<String> childspartecipant = new ArrayList<String>();
		childspartecipant.addAll(mCRL2.childTaskProcess(((PartecipantProcess) mcrl.getPartcipant(name)).getProcess(),
				mcrl, childspartecipant));
		for (String s : childspartecipant) {
			for (Action ab : mcrl.getActions()) {
				if (ab.getName().equals(s) && ab.getPet() != null && ab.getPet().equals(PETLabel.SSRECONTRUCTION))
					return true;
			}
		}

		return false;
	}
}
