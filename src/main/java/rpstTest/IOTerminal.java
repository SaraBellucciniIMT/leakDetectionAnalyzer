/**
 * 
 */
package rpstTest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.bpmn.BpmnControlFlow;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
	private final static String evidencelps = ".pbes.evidence.lps";
	private final static String evidencelts = ".pbes.evidence.lts";
	private final static String evidencefsm = ".pbes.evidence.fsm";
	private File dir = new File("result_FSAT");
	private URI dirname;
	private String mcrl2file;

	public IOTerminal() {
		if (!dir.exists())
			dir.mkdir();
		dirname = dir.toURI();
		System.out.println("Insert file name");
		scan = new Scanner(System.in);
		String inputfile = scan.nextLine();
		File f = new File(inputfile);
		String filename = f.getName().substring(0, f.getName().lastIndexOf("."));
		Pair<Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>, Set<Pair<FlowNode, FlowNode>>> set = null;
		try {
			set = BpmnParser.collaborationParser(inputfile);
			CollaborativeAlg translationalg = new CollaborativeAlg(set);
			mCRL2 mcrl2 = translationalg.getSpec();
			Parout parout = new Parout();
			mcrl2 = parout.parout(mcrl2);
			mcrl2file = mcrl2.toFile(dirname.getPath() + filename);
			String lpsgen = "mcrl22lps " + mcrl2file + " " + mcrl2file.replaceAll(".mcrl2", "") + ".lps";
			runmcrlcommand(lpsgen);

			while (true) {
				String check;
				Set<String> datset = new HashSet<>();
				System.out.println(
						"Select action:\n" + "->1 to check if a <SELECTED> task has a set of <Data1,...,Datan> data \n"
								+ "->2 to check if a <SELECTED> partecipants has a set of  <Data1,...,Datan> data \n"
								+ "->3 Verify if there is a secret sharing violation \n");
				scan = new Scanner(System.in);
				String number = scan.nextLine();
				String partecipant;
				switch (Integer.valueOf(number)) {
				case 1:
					System.out.println(mcrl2.toStringTasks());
					System.out.println("Choose task: ");
					scan = new Scanner(System.in);
					partecipant = scan.nextLine();
					while (!mcrl2.containt(partecipant)) {
						System.out.println("INCORRECT INPUT: TASK DOESN'T EXIST OR THE ELEMENT IS NOT A TASK");
						scan = new Scanner(System.in);
						partecipant = scan.nextLine();
					}
					System.out.println(mcrl2.toStringData());
					datset.addAll(scanData());
					check = TaskFormula.toFile(mcrl2, dirname.getPath(), partecipant, datset, "");
					callFormula(check, filename, mcrl2);
					break;
				case 2:
					System.out.println(mcrl2.toStringPartecipants());
					System.out.println("Choose partecipant: ");
					scan = new Scanner(System.in);
					partecipant = scan.nextLine();
					System.out.println(mcrl2.toStringData());
					datset.addAll(scanData());
					check = PartecipantFormula.toFile(mcrl2, dirname.getPath(), partecipant, datset, "");
					callFormula(check, filename, mcrl2);
					break;
				case 3:
					check = TextInterpreterFormula.toFile(mcrl2, dirname.getPath(), "", datset,
							TextInterpreterFormula.violation);
					callFormula(check, filename, mcrl2);
					break;
				default:
					System.out.println("Operation not recognised");
					System.exit(0);
					break;
				}
				continueOrExit();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void callFormula(String check, String filename, mCRL2 mcrl2) {
		if (check.isEmpty())
			System.err.println("This partecipant/task never have the set of selected elements ");
		else {
			boolean resultbool = lps2pbes2solve2convert(filename, check);
			
			try {
				if(resultbool) {
					List<Pair<String, Set<String>>> s = scanFSMfile(dirname.getPath() + filename + ".pbes.evidence.fsm", mcrl2);
					fromPathInFSMtoJsonFile(dirname.getPath() + "json", s);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String basename = dirname.getPath() + filename;
			deleteTemporaryFile(basename + ".pbes",basename + evidencelps, basename + evidencelts,
					basename + evidencefsm, dirname.getPath() + check);
		}

	}

	private boolean lps2pbes2solve2convert(String filename, String formulafilename) {
		String lps2pbes = "lps2pbes -c -f " + formulafilename + " " + filename + ".lps " + filename + ".pbes";
		runmcrlcommand(lps2pbes);
		String pbessolve = "pbessolve --file=" + filename + ".lps " + filename + ".pbes";
		String resultsolve =runmcrlcommand(pbessolve);
		if(resultsolve.equals("false"))
			return false;
		String lps2lts = "lps2lts " + filename + evidencelps + " " + filename + evidencelts;
		runmcrlcommand(lps2lts);
		String ltsconvert = "ltsconvert " + filename + evidencelts + " " + filename + evidencefsm;
		runmcrlcommand(ltsconvert);
		return true;
	}

	private String runmcrlcommand(String command) {
		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "cd " + dir + " &&" + command);
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
				return line;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();	
		}
		return "";
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
		if (r.equalsIgnoreCase(YES))
			return true;
		else {
			deleteTemporaryFile(dirname.getPath() + mcrl2file, dirname.getPath()  + mcrl2file.replaceAll(".mcrl2", "") + ".lps");
			System.exit(0);
			return false;
		}
	}

	public List<Pair<String, Set<String>>> scanFSMfile(String fileName, mCRL2 mcrl) {
		try {
			FileReader fileReader = new FileReader(fileName);

			// Always wrap FileReader in BufferedReader.
			BufferedReader br = new BufferedReader(fileReader);

			String regex = "[0-9]+ [0-9]+ \"(.)+\"";
			// read line by line
			String line;
			String second = "";
			List<Pair<String,Set<String>>> path  = new ArrayList<Pair<String,Set<String>>>();
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
					Pattern pattern = Pattern.compile("\\{.*\\}");
					TaskProcess t;
					if ((t = TextInterpreterFormula.identifyIdTaskFormula(mcrl, task)) != null) {
						Set<String> tmp = new HashSet<String>();
						Matcher m = pattern.matcher(line);
						while (m.find()) {
							String match = m.group(0).replace("{", "").replace("}", "");
							;
							String[] splitmatch = match.split(",");
							for (String s : splitmatch)
								tmp.add(s);
						}
						path.add(Pair.of(t.getAction().getId(), tmp));
					
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

	private void fromPathInFSMtoJsonFile(String pathfile, List<Pair<String, Set<String>>> path) throws JSONException {
		int i = 0;
		File file = new File(pathfile + i + ".json");
		while (file.exists())
			file = new File(pathfile + (i++) + ".json");

		try (BufferedWriter output = new BufferedWriter(new FileWriter(file))) {
			JSONObject ob = new JSONObject();
			JSONObject obtask = new JSONObject();
			JSONArray  arr = new JSONArray(path);
			for (int j=0; j<path.size(); j++) {
				JSONObject obdata = new JSONObject();
				obdata.append("data", path.get(j).getRight());
				obtask.append(path.get(j).getLeft(), obdata);
			}
			ob.put("path", arr);
			output.write(ob.toString());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void deleteTemporaryFile(String... name) {
		for (String s : name) {
			File f = new File(s);
			if (f.exists())
				f.delete();
		}

	}
}
