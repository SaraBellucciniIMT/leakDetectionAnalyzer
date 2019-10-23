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

import javax.swing.text.Utilities;

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

	public static String toFile(mCRL2 mcrl2, String path, String idname, Set<String> data, String tag) {
		String formula;
		if (identifyIdTaskFormula(mcrl2, idname) != null)
			formula = TaskFormula.generateTaskFormula(mcrl2, idname, data);
		else if (tag.equals(violation))
			formula = sssharingChecking(mcrl2);
		else
			formula = PartecipantFormula.generatePartecipantFormula(mcrl2, idname, data);

		if (formula == null || formula == "-1" || formula.equals(""))
			return formula;

		if (idname.contains(" "))
			idname = idname.replace(" ", "_");
		File file = new File(path + idname + fileName + ".mcf");
		while (file.exists())
			file = new File(path + idname + fileName + (id++) + ".mcf");

		try (BufferedWriter output = new BufferedWriter(new FileWriter(file))) {
			output.write(formula);
			output.close();
		} catch (Exception e) {
			System.err.println("error in generating " + idname + fileName);
		}

		return file.getName();
	}

	private static boolean isPET(PETLabel pet, Map<PET, Set<String>> sensible) {
		for (PET p : sensible.keySet()) {
			if (p.getPET().equals(pet))
				return true;
		}
		return false;
	};

	protected static String sssharingChecking(mCRL2 mcrl2) {
		Map<PET, Set<String>> map = mcrl2.getSensibleData();
		if (map.isEmpty() || (!isPET(PETLabel.SSCOMPUTATION, map) && !isPET(PETLabel.SSRECONTRUCTION, map)
				&& !isPET(PETLabel.SSSHARING, map))) {
			System.out.println("No SSsharing PET over this model");
			return null;
		}
		// check that you can really apply sssharing
		int treshold = 0;
		Set<Set<String>> sssharing = new HashSet<Set<String>>();
		Map<String, Set<String>> groupcomputation = new HashMap<String, Set<String>>();
		Set<String> recontruction = new HashSet<String>();
		for (Entry<PET, Set<String>> entry : map.entrySet()) {
			if (entry.getKey().getPET().equals(PETLabel.SSSHARING)) {
				treshold = ((SSsharing) entry.getKey()).getTreshold();
				sssharing.add(entry.getValue());
			} else if (entry.getKey().getPET().equals(PETLabel.SSCOMPUTATION)) {
				for (String s : entry.getValue()) {
					for (Entry<String, List<String>> groups : ((SScomputation) entry.getKey()).getGroups().entrySet()) {
						if (groups.getValue().contains(s)) {
							if (groupcomputation.containsKey(groups.getKey()))
								groupcomputation.get(groups.getKey()).add(s);
							else
								groupcomputation.put(groups.getKey(), Sets.newHashSet(s));
						}
					}
				}
			} else if (entry.getKey().getPET().equals(PETLabel.SSRECONTRUCTION))
				recontruction.addAll(entry.getValue());
		}
		List<String> formula = new ArrayList<String>();

		Set<PartecipantProcess> partecipantProcesses = mcrl2.getParcipantProcesses();
		Set<PartecipantProcess> remove;
		for (Set<String> ssshares : sssharing) {
			remove = new HashSet<PartecipantProcess>();
			for (PartecipantProcess partecipant : partecipantProcesses) {
				Set<String> datatoanalyze = creationShareParticiapnt(mcrl2, partecipant.getId(), ssshares);
				if (!datatoanalyze.isEmpty()) {
					remove.add(partecipant);
					List<String> tmpformula = getFormulaReconstruction(datatoanalyze, formula, treshold, mcrl2, remove);
					if (!tmpformula.isEmpty()) {
						for (String ff : tmpformula) {
							if (!formula.contains(ff))
								formula.add(ff);
						}
					}
				}

			}
		}

		// -----Computation side-----
		for (Entry<String, Set<String>> entry : groupcomputation.entrySet()) {
			remove = new HashSet<PartecipantProcess>();
			for (PartecipantProcess partecipant : partecipantProcesses) {
				Set<String> datatoanalyze = creationShareParticiapnt(mcrl2, partecipant.getId(), entry.getValue());
				if (!datatoanalyze.isEmpty()) {
					remove.add(partecipant);
					List<String> tmpformula = getFormulaReconstruction(datatoanalyze, formula, treshold, mcrl2, remove);
					if (!tmpformula.isEmpty()) {
						for (String ff : tmpformula) {
							if (!formula.contains(ff))
								formula.add(ff);
						}
					}
				}
			}
		}
		// For reconstruction checking
		if (!recontruction.isEmpty()) {
			partecipantProcesses = mcrl2.getParcipantProcesses();
			remove = new HashSet<PartecipantProcess>();
			for (PartecipantProcess partecipant : partecipantProcesses) {
				if (reconstructionPartecipant(mcrl2, partecipant.getId())
						|| (creationShareParticiapnt(mcrl2, partecipant.getId(), recontruction).isEmpty()))
					remove.add(partecipant);
			}
			partecipantProcesses.removeAll(remove);
			// Every participant that doens't have a reconstruction feaure then, cannot hold
			// more that one info for the reconstruction
			List<String> tmpformula = getFormulaReconstruction(recontruction, formula, treshold, mcrl2,
					partecipantProcesses);
			if (!tmpformula.isEmpty()) {
				for (String ff : tmpformula) {
					if (!formula.contains(ff))
						formula.add(ff);
				}
			}

		}
		String resultformula = "";
		int i = 0;
		for (String f : formula) {
			resultformula = resultformula + f;
			if (i != formula.size() - 1)
				resultformula = resultformula + "||";
			i++;
		}
		if (resultformula.equals(""))
			return null;
		return resultformula;

	}

	protected static List<String> getFormulaReconstruction(Set<String> set, List<String> currentFormula, int treshold,
			mCRL2 mcrl2, Set<PartecipantProcess> partecipantProcesses) {
		Set<Set<String>> listrec = Sets.powerSet(set);
		List<String> formula = currentFormula;
		for (Set<String> d : listrec) {
			if (d.size() == treshold) {
				for (PartecipantProcess partecipant : partecipantProcesses) {
					String f = PartecipantFormula.generatePartecipantFormula(mcrl2, partecipant.getId(), d);
					if (!formula.contains(f))
						formula.add(f);
				}
			}

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
			if (ab.getClass().equals(TaskProcess.class) && ((TaskProcess) ab).getAction() != null
					&& ((TaskProcess) ab).getAction().getId().equals(idtask))
				return ((TaskProcess) ab);
		}
		return null;
	}

	protected static Set<String> generatePar(int n) {
		Set<String> s = new HashSet<String>();
		int i = 0;
		while (n != 0) {
			s.add(parameter + i);
			i = i + 1;
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

	// It return an empty set if it generate both the share use in the computation,
	// null means that the participant is not a generator of share
	private static Set<String> creationShareParticiapnt(mCRL2 mcrl, String name, Set<String> datashares) {
		Set<String> sharenotgenrated = new HashSet<String>();
		if (reconstructionPartecipant(mcrl, name))
			return sharenotgenrated;
		List<String> childspartecipant = new ArrayList<String>();
		childspartecipant.addAll(mCRL2.childTaskProcess(((PartecipantProcess) mcrl.getPartcipant(name)).getProcess(),
				mcrl, childspartecipant));
		for (String s : childspartecipant) {
			for (Action ab : mcrl.getActions()) {
				if (ab.getName().equals(s) && ab.getPet() != null && ab.getPet().equals(PETLabel.SSSHARING)) {
					for (String namep : datashares) {
						if (!ab.containsParameterName(namep))
							sharenotgenrated.add(namep);
					}
					return sharenotgenrated;

				}
			}
		}
		return datashares;
	}
}
