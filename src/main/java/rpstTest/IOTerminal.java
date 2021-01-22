/**
 * 
 */
package rpstTest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.javatuples.Quintet;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.bpmn.BpmnControlFlow;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import algo.CollaborativeAlg;
import formula.TextInterpreterFormula;
import io.BpmnParser;
import io.pet.violation.ParallelViolation;
import io.pet.violation.Reconstruction;
import io.pet.violation.ViolationInterpreter;
import sort.Data;
import sort.Placeholder;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.CommunicationFunction;
import spec.mcrl2obj.MCRL2;
import spec.mcrl2obj.MCRL2Utils;
import spec.mcrl2obj.Processes.TaskProcess;

/**
 * @author sara
 *
 */
public class IOTerminal {
	private static Scanner scan;
	public final static String evidencelts = ".pbes.evidence.lts";
	public final static String evidencefsm = ".pbes.evidence.fsm";
	public final static String op_action = "-a";
	public final static String op_deadlock = "-D";
	public final static String op_trace = "-t";
	public final static String dotmcrl2 = ".mcrl2";
	public final static String dottxt = ".txt";
	public final static String dotlps = ".lps";
	public final static String dotlts = ".lts";
	public final static String json = "json.json";
	public final static String dottrc = ".trc";
	private static File dir = new File("result_FSAT");
	public static URI dirname = dir.toURI();
	// private String mcrl2file;
	private String check;

	public IOTerminal() throws IOException {
		// If the folder result doesn't exist it generates it
		if (!dir.exists())
			dir.mkdir();
		File f;
		while ((f = scanFile()) == null) {
		}
		
		String bpmnFile = FilenameUtils.getBaseName(f.getPath());
		// Parse the BPMN file
		Pair<Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>, Set<Pair<FlowNode, FlowNode>>> setBpmn;
		while ((setBpmn = parsebpmnfile(f)) == null)
			continue;
		// Create the mcrl2 object describing the collaboration
		MCRL2 mcrl2 = new CollaborativeAlg(setBpmn).getSpec();
		String nf;
		while (true) {
			System.out.println(
					"Select action:\n" + "-> 1 Check if a <SELECT> task knows a set of <Data1,...,Datan> data \n"
							+ "-> 2 Check if a <SELECT> partecipants knows a set of  <Data1,...,Datan> data \n"
							+ "-> 3 Verify (SS/AddSS/FunSS/PK/SK) violation \n" + "-> 4 Reconstruction checking\n"
							+ "-> 5 Verify MPC \n" + "-> 6 exit");
			scan = new Scanner(System.in);
			try {
				int n = scan.nextInt();
				switch (n) {
				case 1:
					nf = mcrl2.toFile(dirname.getPath() + bpmnFile);
					mcrl22lps(nf);
					System.out.println(mcrl2.toStringTasks() + "\nChoose task:");
					scan = new Scanner(System.in);
					String task = scan.nextLine();
					while (!mcrl2.containsTask(task)) {
						System.err.println("INCORRECT INPUT: THIS TASK DOESN'T EXIST. CHOOSE ANOTHER ONE: ");
						scan = new Scanner(System.in);
						task = scan.nextLine();
					}
					System.out.println(mcrl2.toStringData());
					Set<Data> data = new HashSet<Data>();
					while ((data = dataexist(mcrl2.getData())) == null) {
					}

					check = TextInterpreterFormula.generateTaskFormula(mcrl2.getActionTask(task), data);
					callFormula(mcrl2, nf);
					break;
				case 2:
					System.out.println(mcrl2.toStringPartecipants() + "\nChoose partecipant:");
					scan = new Scanner(System.in);
					String partcipant = scan.nextLine();
					while (!mcrl2.containsParticipant(partcipant)) {
						System.err.println("INCORRECT INPUT: PARTICIPANT DOESN'T EXIST. CHOOSE ANOTHER ONE: ");
						partcipant = scan.nextLine();
					}
					Data[] dataarray = new Data[0];
					System.out.println(mcrl2.toStringData());
					while ((data = dataexist(mcrl2.getData())) == null) {
					}
					Quintet<Set<Action>, Set<CommunicationFunction>, Set<Placeholder>, Set<String>, Set<TaskProcess>> toremove = ViolationInterpreter
							.detectParty(mcrl2, partcipant, data.toArray(dataarray));
					nf = mcrl2.toFile(dirname.getPath() + bpmnFile);
					mcrl22lps(nf);
					String jsonfile = lps2lts(mcrl2, nf, op_action + MCRL2.CONTAIN.toString(), op_trace + 1);
					if (jsonfile == null)
						System.out.println(partcipant + " does NOT contain the choosen data");
					ViolationInterpreter.rollback(toremove, mcrl2);
					break;
				case 3:
					// Add violation function, if needed to the specification
					toremove = ViolationInterpreter.detectViolation(mcrl2);
					nf = mcrl2.toFile(dirname.getPath() + bpmnFile);
					mcrl22lps(nf);
					jsonfile = lps2lts(mcrl2, nf, op_action + MCRL2.VIOLATION.toString(), op_trace + 1);
					if (jsonfile == null)
						System.out.println("NO VIOLATION OCCURED");
					ViolationInterpreter.rollback(toremove, mcrl2);
					break;
				case 4:
					toremove = new Reconstruction().interpreter(mcrl2);
					if (toremove.getValue0().isEmpty())
						System.out.println("No RECOSTRUCTION TASK");
					else {
						nf = mcrl2.toFile(dirname.getPath() + bpmnFile);
						mcrl22lps(nf);
						String jsonfilerec = lps2lts(mcrl2, nf, op_action + MCRL2.VIOLATION.toString(), op_trace + 1);
						if (jsonfilerec == null)
							System.out.println("Secret ALWAYS reconstructed");
						else
							System.out.println("Secret not reconstructed");
					}
					ViolationInterpreter.rollback(toremove, mcrl2);
					break;
				case 5:
					toremove = new ParallelViolation().interpreter(mcrl2);
					if (toremove.getValue0().isEmpty()) {
						System.out.println("No MPC task");
					} else {
						nf = mcrl2.toFile(dirname.getPath() + bpmnFile);
						mcrl22lps(nf);
						String jsonfilerec = lps2lts(mcrl2, nf, op_deadlock, op_trace);
						if (jsonfilerec == null)
							System.out.println("Parallelism preserved");
						else
							System.out.println("Parallelism is not preserved");
					}
					ViolationInterpreter.rollback(toremove, mcrl2);
					break;
				case 6:
					cleanDirectory();
					System.exit(0);
				default:
					System.out.println("OPERATION NOT RECOGNIZED");
				}
			} catch (Exception e) {
				System.err.print(e.toString());
			}
		}
	}

	private File scanFile() {
		System.out.println("Insert file path");
		scan = new Scanner(System.in);
		String inputfile = scan.nextLine();
		if (!FilenameUtils.isExtension(inputfile, "bpmn"))
			inputfile = inputfile.concat(".bpmn");
		File f = new File(inputfile);
		if (!f.exists()) {
			System.out.println("File not found, try again...");
			return null;
		}
		return f;
	}

	/**
	 * Cleans the directory from all the files in it
	 */
	private void cleanDirectory() {
		for (File file : dir.listFiles()) {
			if (!file.isDirectory())
				file.delete();
		}
	}

	/**
	 * Returns the set bpmn parties in the collaboration and the associated set of
	 * messages between their flownodes
	 * 
	 * @param f the .bpmn file to parse
	 * @return the set bpmn parties in the collaboration and the associated set of
	 *         messages between their flownodes
	 */
	public static Pair<Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>, Set<Pair<FlowNode, FlowNode>>> parsebpmnfile(
			File f) {
		Pair<Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>, Set<Pair<FlowNode, FlowNode>>> set = null;
		try {
			set = BpmnParser.collaborationParser(f);
		} catch (IOException e) {
			e = new FileNotFoundException("Can't parse this file");
			return null;
		}

		return set;
	}

	/**
	 * Returns null if all the input data are in the specification, otherwise the
	 * data that has not been found
	 * 
	 * @param dataMcrl2 the set of data to check
	 * @return null if all the input data are in the specification, otherwise the
	 *         data that has not been found
	 */
	private Set<Data> dataexist(Set<Data> dataMcrl2) {
		Set<Data> scannedData = new HashSet<Data>();
		for (String id : scanData()) {
			boolean found = false;
			for (Data d : dataMcrl2) {
				if (d.equalName(id)) {
					scannedData.add(d);
					found = true;
					break;
				}
			}
			if (!found) {
				System.err.println(id + "is not a data in the model");
				return null;
			}
		}
		return scannedData;
	}

	/**
	 * It transform the mcrl2 in an lps and verify the mcf formula over it using the
	 * pbes, the counterexample is then tranformed from lts to fsm and a json file
	 * is generated form it
	 * 
	 * @param mcrl2
	 */
	private void callFormula(MCRL2 mcrl2, String nameFile) {
		// long startTime = getCurrentTime();
		boolean resultbool = lps2pbes2solve2convert(nameFile);
		System.out.println(resultbool);
		if (resultbool && new File(dirname.getPath() + nameFile + evidencefsm).exists()) {
			List<Pair<String, Set<String>>> s = scanFSMfile(dirname.getPath() + nameFile + evidencefsm, mcrl2);
			try {
				fromPathInFSMtoJsonFile(s, nameFile);
			} catch (JSONException e) {
				new JSONException("File not generated");
			}

		} else
			System.out.println("No JSON file generated because there isn't a path to show");

		// long endTime = getCurrentTime();
		// System.out.println("Verification time: " + computeTimeSpans(startTime,
		// endTime) + " s");
	}

	/**
	 * Transform a ".mcrl2" file into a ".lps" file
	 */
	private void mcrl22lps(String nameFile) {
		String mcrl22lps = "mcrl22lps " + nameFile + dotmcrl2 + " " + nameFile + dotlps;
		runmcrlcommand(mcrl22lps);
	}

	/**
	 * Returns the name of the json file containing the path leading to the
	 * violation of a formula
	 * 
	 * @param mcrl2 the mcrl2 specification on which we are searching for the
	 *              violation
	 * 
	 * @return the name of the json file containing the path leading to the
	 *         violation of a formula, otherwise null
	 */
	private String lps2lts(MCRL2 mcrl2, String nameFile, String... option) {
		String lps2lts = "lps2lts ";
		Boolean deadlock = false;
		for (int i = 0; i < option.length; i++) {
			lps2lts += option[i] + " ";
			if (option[i].equals(op_deadlock))
				deadlock = true;
		}
		lps2lts += nameFile + dotlps + Utils.space + nameFile + dotlts;
		runmcrlcommand(lps2lts);

		List<String> path = new ArrayList<String>();
		String[] children = dir.list();
		List<Pair<String, Set<String>>> path_tracepp = new ArrayList<Pair<String, Set<String>>>();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				// Get filename of file or directory
				String filename = children[i];
				if (FilenameUtils.getBaseName(filename).contains(nameFile + dotlps)
						&& FilenameUtils.isExtension(filename, "trc")) {
					String tracepp = "tracepp " + filename + " " + filename.replace(dottrc, dottxt);
					runmcrlcommand(tracepp);
					FileReader fileReader;
					try {
						fileReader = new FileReader(dirname.getPath() + filename.replace(dottrc, dottxt));
						BufferedReader br = new BufferedReader(fileReader);
						String line;
						while ((line = br.readLine()) != null) {
							path.add(line);
						}
						br.close();
					} catch (IOException e) {
						e = new IOException("File " + dottrc + "not found");
					}
					path_tracepp = generateList(path, mcrl2);
					if (!path.isEmpty() && deadlock) {
						String lasttask = path_tracepp.get(path_tracepp.size() - 1).getKey();
						if (!lasttask.equals(MCRL2.TAU.toString()) && !mcrl2.containsTask(lasttask)) {
							path_tracepp = new ArrayList<Pair<String, Set<String>>>();
							continue;
						}
					}
					break;
				}
			}
		}
		String jsonfilename = null;
		if (!path_tracepp.isEmpty()) {
			try {
				jsonfilename = fromPathInFSMtoJsonFile(path_tracepp, nameFile);
			} catch (JSONException e) {
				e = new JSONException("Error in writing the path");
			}
		}
		return jsonfilename;
	}

	/**
	 * Generate a list of couples <taskname, set<data>>
	 * 
	 * @param path  the list of tasks executed
	 * @param mcrl2 the mcrl2 specification under consideration
	 * @return a list of couples <taskname, set<data>>
	 */
	private List<Pair<String, Set<String>>> generateList(List<String> path, MCRL2 mcrl2) {
		List<Pair<String, Set<String>>> list = new ArrayList<Pair<String, Set<String>>>();
		for (int i = 0; i < path.size(); i++) {
			String el = path.get(i);
			if (el.equals(MCRL2.TAU.toString()))
				continue;
			// System.out.println(el);
			Set<String> setdata = new HashSet<String>();
			Pattern ptk = Pattern.compile("\\(\\{(.*)\\}\\)");
			String nametask = el.replaceAll(ptk.pattern(), "");
			Matcher m = ptk.matcher(el);
			if (m.find()) {
				String datas = m.group(1);
				int brakets = 0;
				int startindex = 0;
				for (int j = 0; j < datas.length(); j++) {
					if ((String.valueOf(datas.charAt(j)).equals(",") && brakets == 0) || (j == datas.length() - 1)) {
						Data d;
						if ((j == datas.length() - 1))
							d = Data.detectData(datas.substring(startindex, j + 1), mcrl2.getData());
						else
							d = Data.detectData(datas.substring(startindex, j), mcrl2.getData());
						// System.out.println(d.getRealName());
						setdata.add(d.getRealName());
						startindex = j + 1;
					} else if (String.valueOf(datas.charAt(j)).equals("(")) {
						brakets++;
					} else if (String.valueOf(datas.charAt(j)).equals(")")) {
						brakets--;
					}
				}
			} else
				nametask = el;

			list.add(Pair.of(nametask, setdata));
		}
		return list;
	}

	private boolean lps2pbes2solve2convert(String nameFile) {
		String lps2lts = "lps2lts " + nameFile + dotlps + " " + nameFile + dotlts;
		runmcrlcommand(lps2lts);
		String ltsconvert = "ltsconvert -etau-star " + nameFile + dotlts + " " + nameFile + dotlts;
		runmcrlcommand(ltsconvert);
		String lts2pbes = "lts2pbes -c -f " + check + " " + nameFile + dotlts + " " + nameFile + ".pbes";
		runmcrlcommand(lts2pbes);
		String pbessolve = "pbessolve -s1 --file=" + nameFile + dotlts + " " + nameFile + ".pbes";
		String resultsolve = runmcrlcommand(pbessolve);
		String convert = "ltsconvert " + nameFile + evidencelts + " " + nameFile + evidencefsm;
		runmcrlcommand(convert);
		if (Boolean.valueOf(resultsolve))
			return true;
		else
			return false;
	}

	/**
	 * Run the given command of the cmd, returns the output printed on the command
	 * 
	 * @param command the command to be run
	 * @return the output printed on the command
	 */
	private String runmcrlcommand(String command) {
		boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
		ProcessBuilder builder = new ProcessBuilder();
		if (isWindows) {
			builder.command("cmd.exe", "/c", "cd " + dir + " &&" + command);
		} else {
			builder.command("sh", "-c", "cd " + dir + " &&" + command);
		}
		builder.redirectErrorStream(true);
		Process p;
		try {
			p = builder.start();
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String lastline = null;
			String currentline;
			while (true) {
				currentline = r.readLine();
				if (currentline == null)
					break;
				lastline = currentline;
			}
			return lastline;
		} catch (IOException e) {
			e = new IOException();
		}
		return "";
	}

	private Set<String> scanData() {
		Set<String> datset = new HashSet<>();
		System.out.println("Choose data (, in the middle): ");
		scan = new Scanner(System.in);
		String s = scan.nextLine();
		String[] split = s.split(",");
		for (int i = 0; i < split.length; i++) {
			datset.add(split[i]);
		}
		return datset;
	}

	private List<Pair<String, Set<String>>> scanFSMfile(String fileName, MCRL2 mcrl) {
		try {
			FileReader fileReader = new FileReader(fileName);
			// Always wrap FileReader in BufferedReader.
			BufferedReader br = new BufferedReader(fileReader);
			String regex = "[0-9]+ [0-9]+ \"(.)+\"";
			// read line by line
			String line;
			Map<Pair<Integer, Integer>, Pair<String, Set<String>>> map = new HashedMap<Pair<Integer, Integer>, Pair<String, Set<String>>>();
			Pair<Integer, Integer> root = null;
			while ((line = br.readLine()) != null) {
				if (line.matches(regex)) {
					String[] split = line.split(" ");
					if (root == null)
						root = Pair.of(Integer.valueOf(split[0]), Integer.valueOf(split[1]));
					String task = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\"")).replaceAll("\\(.*\\)",
							"");
					TaskProcess t;
					Set<String> tmp = new HashSet<String>();
					if (task.equals(MCRL2.TAU.getId()))
						map.put(Pair.of(Integer.valueOf(split[0]), Integer.valueOf(split[1])),
								Pair.of(MCRL2.TAU.getId(), tmp));
					if ((t = mcrl.getActionTask(task)) != null) {
						Set<String> setData = MCRL2Utils.parseFSMTaskLine(line);
						for (Data d : mcrl.getData()) {
							for (String s : setData) {
								if (d.getId().equals(s))
									tmp.add(d.getRealName());
							}
						}
						map.put(Pair.of(Integer.valueOf(split[0]), Integer.valueOf(split[1])),
								Pair.of(t.getAction().getId(), tmp));
					}
				}
			}
			br.close();
			return recognizePath(map, root);
		} catch (IOException e) {
			new FileNotFoundException();
		}
		return null;
	}

	private List<Pair<String, Set<String>>> recognizePath(Map<Pair<Integer, Integer>, Pair<String, Set<String>>> map,
			Pair<Integer, Integer> root) {
		List<Pair<String, Set<String>>> path = new ArrayList<Pair<String, Set<String>>>();
		path.add(map.get(root));
		Integer lastnum = root.getRight();
		boolean find = true;
		while (find) {
			find = false;
			for (Entry<Pair<Integer, Integer>, Pair<String, Set<String>>> entrytwo : map.entrySet()) {
				if (entrytwo.getKey().getLeft().equals(lastnum)) {
					lastnum = entrytwo.getKey().getRight();
					path.add(entrytwo.getValue());
					find = true;
					break;
				}
			}
		}

		return path;
	}

	/**
	 * Generate a json file from the given path
	 * 
	 * @param path the path that we want to transform in a json file
	 * @throws JSONException if the file to write the json is not found
	 */
	private String fromPathInFSMtoJsonFile(List<Pair<String, Set<String>>> path, String nameFile) throws JSONException {
		System.out.println("PATH : " + path.toString());
		File file = new File(nameFile + json);
		int i = 0;
		while (file.exists())
			file = new File(nameFile + i++ + IOTerminal.dotmcrl2);
		try (BufferedWriter output = new BufferedWriter(new FileWriter(file))) {
			JSONObject ob = new JSONObject();
			JSONObject obtask = new JSONObject();
			JSONArray arr = new JSONArray(path);
			for (int j = 0; j < path.size(); j++) {
				JSONObject obdata = new JSONObject();
				obdata.append("data", path.get(j).getRight());
				obtask.append(path.get(j).getLeft(), obdata);
			}
			ob.put("path", arr);
			output.write(ob.toString());
		} catch (IOException e) {
			new FileNotFoundException();
		}
		return file.getName();
	}

	private long getCurrentTime() {
		return System.nanoTime();
	}

	private long computeTimeSpans(long startTime, long endTime) {
		return TimeUnit.NANOSECONDS.toSeconds(endTime - startTime);
	}

}
