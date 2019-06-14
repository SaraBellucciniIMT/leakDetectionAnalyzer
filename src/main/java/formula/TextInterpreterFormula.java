/**
 * 
 */
package formula;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.print.attribute.SetOfIntegerSyntax;

import java.util.Set;

import org.apache.commons.collections4.SetUtils;
import org.jbpt.pm.DataNode;

import com.google.common.collect.Sets;

import io.pet.PET;
import io.pet.PETLabel;
import io.pet.SScomputation;
import io.pet.SSreconstruction;
import io.pet.SSsharing;
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

	public static void toFile(mCRL2 mcrl2, String name, Set<String> data, String violation) {
		String path = "C:\\Users\\sara\\eclipse-workspace\\rpstTest\\result\\";

		File file = new File(path + fileName + ".mcf");
		while (file.exists())
			file = new File(path + fileName + (id++) + ".mcf");

		try (BufferedWriter output = new BufferedWriter(new FileWriter(file))) {
			if (violation.equals(TextInterpreterFormula.violation)) {
				output.write(sssharingChecking(mcrl2, name));
			} else {
				if (mcrl2.identifyProcess(name) != null || mcrl2.identifyTaskProcess(name) != null)
					output.write(TaskFormula.generateTaskFormula(mcrl2, name, data));
				else
					output.write(PartecipantFormula.generatePartecipantFormula(mcrl2, name, data));
			}
			System.out.println(fileName + " GENERATED");
		} catch (Exception e) {
			System.err.println("error in generating " + fileName);
		}
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
		formula = formula + "||";
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

	
}
