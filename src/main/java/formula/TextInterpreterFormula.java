/**
 * 
 */
package formula;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;
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
	protected static final String openallformula = "[true*.";
	protected static final String closeallformula = "]true";
	private static final String fileName = "formula";
	private static int id = 0;
	private static String parameter = "p";

	public static String generateParticipantFormula(mCRL2 mcrl2, String path, String idname, Set<String> data) {
		String formula = PartecipantFormula.generatePartecipantFormula(mcrl2, idname, data);
		return generateMCFfile(formula, path);
	}

	public static String generateLivenessFormula(String dirname) {
		String formula = openpossibilityformula + mCRL2.recostruct + closepossibilityformula;
		return generateMCFfile(formula, dirname);
	}
	
	public static String generateSaferyFormula(String dirname) {
		String 	formula = openpossibilityformula + mCRL2.violation + closepossibilityformula;
		return generateMCFfile(formula, dirname);
	}
	
	public static String generateTaskFormula(mCRL2 mcrl2, String path, String idname, Set<String> data) {
		String formula = TaskFormula.generateTaskFormula(mcrl2, identifyIdTaskFormula(mcrl2, idname), data,
				openpossibilityformula, closepossibilityformula);
		return generateMCFfile(formula, path);
	}
	private static String generateMCFfile(String formula, String dirname) {
		if (formula == null || formula.equals("") || formula.equals("-1"))
			return formula;
		File file = new File(dirname + fileName + ".mcf");
		while (file.exists())
			file = new File(dirname + fileName + (id++) + ".mcf");
		try (BufferedWriter output = new BufferedWriter(new FileWriter(file))) {
			output.write(formula);
			output.close();
		} catch (Exception e) {
			System.err.println("error in generating formula:" + fileName);
		}
		return file.getName();
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

}
