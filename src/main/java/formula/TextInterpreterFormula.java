/**
 * 
 */
package formula;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.google.common.collect.Sets;

import io.pet.PET;
import io.pet.PETLabel;
import io.pet.SScomputation;
import io.pet.SSsharing;
import spec.mcrl2obj.AbstractProcess;
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
	public static final String violation = "violation";
	private static String parameter = "p";

	public static String toFile(mCRL2 mcrl2, String idname, Set<String> data) {
		String path = "C:\\Users\\sara\\eclipse-workspace\\rpstTest\\result\\";
		File file = new File(path + idname + fileName + ".mcf");
		while (file.exists())
			file = new File(path + idname +fileName + (id++) + ".mcf");

		try (BufferedWriter output = new BufferedWriter(new FileWriter(file))) {

				if (identifyIdTaskFormula(mcrl2, idname) != null)
					output.write(TaskFormula.generateTaskFormula(mcrl2, idname, data));
				else
					output.write(PartecipantFormula.generatePartecipantFormula(mcrl2, idname, data));
			
			output.close();
			System.out.println(file.getName() + " GENERATED");
		} catch (Exception e) {
			System.err.println("error in generating " + idname +fileName);
		}
	
		return file.getName();
	}

	public static String sssharingChecking(mCRL2 mcrl2, String partecipantname) {
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
		for (Entry<Integer, Set<String>> entry : groupcomputation.entrySet()) {
			Set<Set<String>> list = Sets.powerSet(entry.getValue());

			for (Set<String> d : list) {
				if (d.size() == treshold) {
					if (!formula.isEmpty())
						formula = formula + "||";
					formula = formula + PartecipantFormula.generatePartecipantFormula(mcrl2, partecipantname,
							new HashSet<String>(d));
				}
			}
		}
		formula = "(" + formula+ ")" + "||";
		Set<Set<String>> subrec = Sets.powerSet(recontruction);
		String subformula = "";
		for (Set<String> d : subrec) {
			if (d.size() == treshold) {
				if (!subformula.isEmpty())
					subformula = subformula + "||";
				subformula = subformula
						+ PartecipantFormula.generatePartecipantFormula(mcrl2, partecipantname, new HashSet<String>(d));

			}
		}
		formula = formula + subformula;
		return formula;
	}

	protected static TaskProcess identifyTaskFormula(mCRL2 mcrl, String name) {
		for(AbstractProcess ab : mcrl.getProcesses()) {
			if(ab.getClass().equals(TaskProcess.class) && ((TaskProcess)ab).getAction().getName().equals(name))
				return (TaskProcess)ab;
		}
		return null;
	}
	
	public static TaskProcess identifyIdTaskFormula(mCRL2 mcrl, String idtask) {
		for(AbstractProcess ab : mcrl.getProcesses()) {
			if(ab.getClass().equals(TaskProcess.class) && ((TaskProcess)ab).getAction().getId().equals(idtask))
				return ((TaskProcess)ab);
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
}
