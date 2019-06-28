/**
 * 
 */
package rpstTest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.bpmn.BpmnControlFlow;

import algo.CollaborativeAlg;
import formula.PartecipantFormula;
import formula.TaskFormula;
import formula.TextInterpreterFormula;
import io.BpmnParser;
import pcrrlalgoelement.Parout;
import spec.mcrl2obj.TaskProcess;
import spec.mcrl2obj.mCRL2;

/**
 * @author sara
 *
 */
public class IOTerminal {
	private static Scanner scan;
	private final static String YES = "Y";
	private final static String NO = "N";

	public IOTerminal() {
		while (true) {

			System.out.println("Inser filename : ");
			scan = new Scanner(System.in);
			String filename = scan.nextLine();
			String pathfile = "C:\\Users\\sara\\eclipse-workspace\\rpstTest\\bpmnfile\\";
			String pathresult = "C:\\Users\\sara\\eclipse-workspace\\rpstTest\\result\\";
			Pair<Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>, Set<Pair<FlowNode, FlowNode>>> set = null;
			try {
				set = BpmnParser.collaborationParser(pathfile + filename + ".bpmn");

				CollaborativeAlg translationalg = new CollaborativeAlg(set);
				mCRL2 mcrl2 = translationalg.getSpec();
				Parout parout = new Parout();
				mcrl2 = parout.parout(mcrl2);
				String mcrl2file = mcrl2.toFile(filename);
				String lpsgen = "mcrl22lps " + mcrl2file + " " + mcrl2file.replaceAll(".mcrl2", "") + ".lps";
				runmcrlcommand(lpsgen);

				while (continueOrExit()) {
					String check;
					Set<String> datset = new HashSet<>();
					System.out.println("Select action: \n "
							+ "->1 to check if a <SELECTED> task has a set of <Data1,...,Datan> data \n"
							+ "->2 to check if a <SELECTED> partecipants has a set of  <Data1,...,Datan> data \n"
							+ "->3 Verify if there is a secret sharing violation \n"
							+ "->4 Verify if there is an ecryption violation");
					scan = new Scanner(System.in);
					String number = scan.nextLine();
					String partecipant;
					switch (Integer.valueOf(number)) {
					case 1:
						System.out.println(mcrl2.toStringTasks());
						System.out.println("Choose task: ");
						scan = new Scanner(System.in);
						partecipant = scan.nextLine();
						System.out.println(mcrl2.toStringData());
						datset.addAll(scanData());
						check = TaskFormula.toFile(mcrl2, partecipant, datset);
						if (check.isEmpty())
							System.err.println("This partecipant never have the set of selected elements");
						else {
							System.out.println("Start verification...");
							lps2pbes2solve2convert(filename, check);
							String s = scanFSMfile(pathresult + filename + ".pbes.evidence.fsm", mcrl2);
							System.out.println("PATH: " + s);
						}

						break;
					case 2:
						System.out.println(mcrl2.toStringPartecipants());
						System.out.println("Choose partecipant: ");
						scan = new Scanner(System.in);
						partecipant = scan.nextLine();
						System.out.println(mcrl2.toStringData());
						datset.addAll(scanData());
						check = PartecipantFormula.toFile(mcrl2, partecipant, datset);
						if (check.isEmpty())
							System.err.println("This partecipant never have the set of selected elements");
						else {
							System.out.println("Start verification...");
							lps2pbes2solve2convert(filename, check);
							System.out.println(
									"PATH: " + scanFSMfile(pathresult + filename + ".pbes.evidence.fsm", mcrl2));
						}
						break;
					case 3:
						//TextInterpreterFormula.sssharingChecking(mcrl2, partecipantname)
					case 4:
						System.exit(0);
					default:
						break;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void lps2pbes2solve2convert(String filename, String formulafilename) {
		String lps2pbes = "lps2pbes -c -f " + formulafilename + " " + filename + ".lps " + filename + ".pbes";
		runmcrlcommand(lps2pbes);
		System.out.println("pbes generater");
		String pbessolve = "pbessolve --file=" + filename + ".lps " + filename + ".pbes";
		runmcrlcommand(pbessolve);
		System.out.println("counter example generated");
		String lps2lts = "lps2lts " + filename + ".pbes.evidence.lps " + filename + ".pbes.evidence.lts";
		runmcrlcommand(lps2lts);
		System.out.println("lts generated");
		String ltsconvert = "ltsconvert " + filename + ".pbes.evidence.lts " + filename + ".pbes.evidence.fsm";
		runmcrlcommand(ltsconvert);
		System.out.println("lts conveterd into fsm file");
	}

	private boolean runmcrlcommand(String command) {
		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c",
				"cd \"C:\\Users\\sara\\eclipse-workspace\\rpstTest\\result\" &&" + command);
		builder.redirectErrorStream(true);
		Process p;
		try {
			p = builder.start();

			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while (true) {
				line = r.readLine();
				if (line == null) {
					break;
				}
				System.out.println(line);
			}
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	private Set<String> scanData() {
		Set<String> datset = new HashSet<>();
		System.out.println("Choose data (, in the middle): ");
		scan = new Scanner(System.in);
		String s = scan.nextLine();
		String[] split = s.split(",");
		for (int i = 0; i < split.length; i++)
			datset.add(split[i]);

		return datset;
	}

	private boolean continueOrExit() {
		System.out.println("CONTINUE (Y/N) : ");
		String r = scan.nextLine();
		while (!r.equalsIgnoreCase(NO) && !r.equalsIgnoreCase(YES)) {
			System.out.println("command not available, try again ... ");
			r = scan.nextLine();
		}
		if (r.equals(YES))
			return true;
		if (r.equalsIgnoreCase(NO))
			System.exit(0);
		return false;
	}

	public String scanFSMfile(String fileName, mCRL2 mcrl) {
		try {
			FileReader fileReader = new FileReader(fileName);

			// Always wrap FileReader in BufferedReader.
			BufferedReader br = new BufferedReader(fileReader);

			String regex = "[0-9]+ [0-9]+ \"(.)+\"";
			// read line by line
			String line;
			String path = "";
			String second = "";
			while ((line = br.readLine()) != null) {
				if (line.matches(regex)) {
					String[] split = line.split(" ");
					if (second.isEmpty()) {
						second = split[0];
					} else if (!second.equals(split[0]))
						continue;
					second = split[1];
					String task = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\"")).replaceAll("\\(.*\\)",
							"");
					TaskProcess t;
					if ((t = TextInterpreterFormula.identifyIdTaskFormula(mcrl, task)) != null) {
						if (!t.getAction().getSecondName().isEmpty())
							path = path + " " + t.getAction().getSecondName();
						else
							path = path + " " + task;
					}
				}
			}
			br.close();
			return path;

		} catch (IOException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
		}

		return null;

	}
}
