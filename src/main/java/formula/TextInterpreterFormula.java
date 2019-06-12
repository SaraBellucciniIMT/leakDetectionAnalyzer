/**
 * 
 */
package formula;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Set;

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

	public static void toFile(mCRL2 mcrl2, String name, Set<String> data) {
		String path = "C:\\Users\\sara\\eclipse-workspace\\rpstTest\\result\\";
		
		File file = new File(path + fileName + ".mcf");
		while (file.exists())
			file = new File(path + fileName + (id++) + ".mcf");

		try (BufferedWriter output = new BufferedWriter(new FileWriter(file))) {
			if (mcrl2.identifyProcess(name) != null)
				output.write(TaskFormula.generateTaskFormula(mcrl2, name, data));
			else
				output.write(PartecipantFormula.generatePartecipantFormula(mcrl2, name, data));
			System.out.println(fileName + " GENERATED");
		} catch (Exception e) {
			System.err.println("error in generating " + fileName);
		}
	}
}
