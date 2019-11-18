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
	private static final String fileName = "formula";
	private static int id = 0;
	private static String parameter = "p";

	public static String toFile(mCRL2 mcrl2, String path, String idname, Set<String> data, String tag) {
		String formula;
		if (identifyIdTaskFormula(mcrl2, idname) != null)
			formula = TaskFormula.generateTaskFormula(mcrl2, idname, data);
		else
			formula = PartecipantFormula.generatePartecipantFormula(mcrl2, idname, data);

		if (formula == null || formula.equals(""))
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
