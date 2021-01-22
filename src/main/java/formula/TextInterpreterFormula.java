/**
 * 
 */
package formula;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

import rpstTest.IOTerminal;
import sort.Data;
import spec.mcrl2obj.Processes.TaskProcess;

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

	public static String generateLivenessFormula(String action) {
		String formula = openpossibilityformula + action + closepossibilityformula;
		return generateMCFfile(formula);
	}
	
	public static String generateSaferyFormula(String action) {
		String 	formula = openpossibilityformula + action + closepossibilityformula;
		return generateMCFfile(formula);
	}
	
	public static String generateTaskFormula(TaskProcess t, Set<Data> data) {
		String formula = TaskFormula.generateTaskFormula(t, data,
				openpossibilityformula, closepossibilityformula);
		return generateMCFfile(formula);
	}
	
	public static String generateMCFfile(String formula) {
		if (formula == null || formula.equals("") || formula.equals("-1"))
			return formula;
		File file = new File(IOTerminal.dirname.getPath() + fileName + ".mcf");
		while (file.exists())
			file = new File(IOTerminal.dirname.getPath() + fileName + (id++) + ".mcf");
		try (BufferedWriter output = new BufferedWriter(new FileWriter(file))) {
			output.write(formula);
			output.close();
		} catch (Exception e) {
			System.err.println(e.toString());
		}
		return file.getName();
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
