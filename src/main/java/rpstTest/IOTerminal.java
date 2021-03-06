/**
 * 
 */
package rpstTest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
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
import org.json.JSONString;
import org.json.JSONStringer;
import org.json.JSONWriter;

import com.google.common.io.Files;

import algo.CollaborativeAlg;
import formula.TextInterpreterFormula;
import io.BpmnParser;
import io.DotBPMNKeyW;
import io.pet.PETLabel;
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
		long startTime = getCurrentTime();
		MCRL2 mcrl2 = new CollaborativeAlg(setBpmn).getSpec();
		// Time to parse the bpmn file and create the mcrl2 object
		long intermediateTime = computeTimeSpans(startTime);
		long startTime2;
		long stver;
		String nf;
		while (true) {
			System.out.println(
					"Select action:\n" + "-> 1 Check if a <SELECT> task knows a set of <Data1,...,Datan> data \n"
							+ "-> 2 Check if a <SELECT> partecipants knows a set of  <Data1,...,Datan> data \n"
							+ "-> 3 Verify (SS/AddSS/FunSS/PK/SK) violation \n" + "-> 4 Reconstruction checking\n"
							+ "-> 5 Verify MPC \n" + "-> 6 Deadlock freedom \n" + "-> 7 exit");
			scan = new Scanner(System.in);
			try {
				int n = scan.nextInt();
				switch (n) {
				case 1:
					startTime2 = getCurrentTime();
					nf = mcrl2.toFile(dirname.getPath() + bpmnFile);
					//System.out.println("Translation time: " + computeTimeSpans(startTime2) + intermediateTime);
					stver = getCurrentTime();
					mcrl22lps(nf);
					System.out.println(mcrl2.toStringTasks() + "\nSelect task:");
					scan = new Scanner(System.in);
					String task = scan.nextLine();
					while (!mcrl2.containsTask(task)) {
						System.err.println("INCORRECT INPUT: THIS TASK DOESN'T EXIST. Try again: ");
						scan = new Scanner(System.in);
						task = scan.nextLine();
					}
					System.out.println("\n" + mcrl2.toStringData());
					Set<Data> data = new HashSet<Data>();
					while ((data = dataexist(mcrl2.getData())) == null) {
					}

					check = TextInterpreterFormula.generateTaskFormula(mcrl2.getActionTask(task), data);
					callFormula(mcrl2, nf);
					break;
				case 2:
					System.out.println(mcrl2.toStringPartecipants() + "\nSelect partecipant:");
					scan = new Scanner(System.in);
					String partcipant = scan.nextLine();
					while (!mcrl2.containsParticipant(partcipant)) {
						System.err.println("INCORRECT INPUT: PARTICIPANT DOESN'T EXIST. Try again: ");
						partcipant = scan.nextLine();
					}
					Data[] dataarray = new Data[0];
					System.out.println(mcrl2.toStringData());
					while ((data = dataexist(mcrl2.getData())) == null) {
					}
					startTime2 = getCurrentTime();
					Quintet<Set<Action>, Set<CommunicationFunction>, Set<Placeholder>, Set<String>, Set<TaskProcess>> toremove = ViolationInterpreter
							.detectParty(mcrl2, partcipant, data.toArray(dataarray));
					nf = mcrl2.toFile(dirname.getPath() + bpmnFile);
					//System.out.println("Translation time: " + computeTimeSpans(startTime2) + intermediateTime);
					mcrl22lps(nf);
					String jsonfile = lps2lts(mcrl2, nf, op_action + MCRL2.CONTAIN.toString(), op_trace + 1);
					if (jsonfile == null)
						System.out.println(partcipant + " does NOT contain the selected data");
					ViolationInterpreter.rollback(toremove, mcrl2);
					break;
				case 3:
					startTime2 = getCurrentTime();
					// Add violation function, if needed to the specification
					toremove = ViolationInterpreter.detectViolation(mcrl2);
					nf = mcrl2.toFile(dirname.getPath() + bpmnFile);
					//System.out.println("Translation time: " + computeTimeSpans(startTime2) + intermediateTime);
					stver = getCurrentTime();
					mcrl22lps(nf);
					jsonfile = lps2lts(mcrl2, nf, op_action + MCRL2.VIOLATION.getId(), op_trace + 1);
					if (jsonfile == null)
						System.out.println("NO VIOLATION OCCURED");
					//System.out.println("Verification time: " + computeTimeSpans(stver));
					ViolationInterpreter.rollback(toremove, mcrl2);
					break;
				case 4:
					startTime2 = getCurrentTime();
					toremove = new Reconstruction().interpreter(mcrl2);
					if (toremove.getValue0().isEmpty()) {
						System.out.println("NO RECOSTRUCTION TASK");
						//System.out.println("Translation time: " + computeTimeSpans(startTime2) + intermediateTime);
					} else {
						nf = mcrl2.toFile(dirname.getPath() + bpmnFile);
						//System.out.println("Translation time: " + computeTimeSpans(startTime2) + intermediateTime);
						stver = getCurrentTime();
						mcrl22lps(nf);
						String jsonfilerec = lps2lts(mcrl2, nf, op_action + MCRL2.VIOLATION.getId(), op_trace + 1);
						if (jsonfilerec == null)
							System.out.println("Secret ALWAYS reconstructed");
						else
							System.out.println("Secret NOT reconstructed");
						//System.out.println("Verification time: " + computeTimeSpans(stver));
					}
					ViolationInterpreter.rollback(toremove, mcrl2);
					break;
				case 5:
					startTime2 = getCurrentTime();
					toremove = new ParallelViolation().interpreter(mcrl2);
					if (toremove.getValue0().isEmpty()) {
						System.out.println("No MPC task");
					//	System.out.println("Translation time: " + computeTimeSpans(startTime2) + intermediateTime);
					} else {
						nf = mcrl2.toFile(dirname.getPath() + bpmnFile);
						//System.out.println("Translation time: " + computeTimeSpans(startTime2) + intermediateTime);
						stver = getCurrentTime();
						mcrl22lps(nf);
						String jsonfilerec = lps2lts(mcrl2, nf, op_deadlock, op_trace);
						if (jsonfilerec == null)
							System.out.println("Parallelism PRESERVED");
						else
							System.out.println("Parallelism is NOT preserved");
						//System.out.println("Verification time: " + computeTimeSpans(stver));
					}
					ViolationInterpreter.rollback(toremove, mcrl2);
					break;
				case 6:
					startTime2 = getCurrentTime();
					nf = mcrl2.toFile(dirname.getPath() + bpmnFile);
					//System.out.println("Translation time: " + computeTimeSpans(startTime2) + intermediateTime);
					stver = getCurrentTime();
					mcrl22lps(nf);
					String jsonf = lps2lts(mcrl2, nf, op_deadlock, op_trace);
					if (jsonf == null)
						System.out.println("NO DEADLOCK");
					//System.out.println("Verification time: " + computeTimeSpans(stver));
					break;
				case 7:
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
		/*
		 * if (!FilenameUtils.isExtension(inputfile, "bpmn")) inputfile =
		 * inputfile.concat(".bpmn");
		 */
		File f = new File(inputfile);
		if (!f.exists()) {
			System.out.println("FILE NOT FOUND, try again...");
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
				System.err.println(id + " is NOT a data object in the model");
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
			List<List<Pair<String, Set<String>>>> s = new ArrayList<>(); 
			s.add(scanFSMfile(dirname.getPath() + nameFile + evidencefsm, mcrl2));
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
	 * 
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

		
		String[] children = dir.list();
		List<List<Pair<String, Set<String>>>> alltracepp = new ArrayList<List<Pair<String, Set<String>>>>();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				// Get filename of file or directory
				List<Pair<String, Set<String>>> onetracepp = new ArrayList<Pair<String, Set<String>>>();
				String filename = children[i];
				List<String> path = new ArrayList<String>();
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
					onetracepp = generateList(path, mcrl2);
					if (!onetracepp.isEmpty()) {
						if (deadlock) {
							if (checkDeadlock(mcrl2.getEndEvents(), onetracepp))
								alltracepp.add(onetracepp);
							else
								continue;
						} else {
							System.out.println(printViolationType(onetracepp.get(onetracepp.size() - 1).getValue(),
									mcrl2.getData()));
							alltracepp.add(onetracepp);
							break;
						}
					}
				}
			}
		}
		String jsonfilename = null;
		if (!alltracepp.isEmpty()) {
			try {
				jsonfilename = fromPathInFSMtoJsonFile(alltracepp, nameFile);
			} catch (JSONException e) {
				e = new JSONException("Error in writing the path");
			}
		}
		return jsonfilename;
	}

	private String printViolationType(Set<String> violationdata, Set<Data> mcrldata) {
		for (Data d : mcrldata) {
			for (String s : violationdata) {
				if (d.getRealName().equals(s) && d.getStereotype() != null) {
					PETLabel l = d.getStereotype();
					if (l.equals(PETLabel.SSSHARING) || l.equals(PETLabel.SSCOMPUTATION))
						return "SECRET SHARING VIOLATED";

					else if (l.equals(PETLabel.CIPHER) || l.equals(PETLabel.DECODINGKEY))
						return "ENCRYPTION VIOLATED";

				}
			}
		}
		return "";
	}

	/**
	 * Returns true if there is a dealdock, otherwise false
	 * 
	 * @param endEvents
	 * @param path
	 * @return
	 */
	private boolean checkDeadlock(Set<String> endEvents, List<Pair<String, Set<String>>> path) {
		List<String> hasevent = new ArrayList<String>();
		for (Pair<String, Set<String>> pair : path) {
			if (endEvents.contains(pair.getKey()))
				hasevent.add(pair.getKey());
		}
		if (hasevent.containsAll(endEvents))
			return false;
		else
			return true;
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
			Set<String> setdata = new HashSet<String>();
			if (el.equals(MCRL2.TAU.toString())) {
				list.add(Pair.of(el, setdata));
				continue;
			}
			// System.out.println(el);
			Pattern ptk = Pattern.compile("\\(\\{(.*)\\}\\)");
			String nametask = "";
			Matcher m = ptk.matcher(el);
			if (m.find()) {
				nametask = el.replaceAll(ptk.pattern(), "");
				setdata.addAll(dataInsideAction(m.group(1), mcrl2.getData()));
			} else if (nametask.isEmpty()) {
				Pattern ptm = Pattern.compile("\\(\\[(.*)\\]\\)");
				Matcher mm = ptm.matcher(el);
				if (mm.find()) {
					nametask = el.replaceAll(ptm.pattern(), "");
					setdata.addAll(dataInsideAction(mm.group(1), mcrl2.getData()));
				}
			} else
				nametask = el;
			list.add(Pair.of(nametask, setdata));
		}
		return list;
	}

	private Set<String> dataInsideAction(String datas, Set<Data> mcrl2data) {
		Set<String> setdata = new HashSet<String>();
		int brakets = 0;
		int startindex = 0;
		for (int j = 0; j < datas.length(); j++) {
			if ((String.valueOf(datas.charAt(j)).equals(",") && brakets == 0) || (j == datas.length() - 1)) {
				Data d;
				if ((j == datas.length() - 1))
					d = Data.detectData(datas.substring(startindex, j + 1), mcrl2data);
				else
					d = Data.detectData(datas.substring(startindex, j), mcrl2data);
				// System.out.println(d.getRealName());
				setdata.add(d.getRealName());
				startindex = j + 1;
			} else if (String.valueOf(datas.charAt(j)).equals("(")) {
				brakets++;
			} else if (String.valueOf(datas.charAt(j)).equals(")")) {
				brakets--;
			}
		}
		return setdata;
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
		System.out.println("Select data object(use \",\" to select more than one data object): ");
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
						if (!setData.isEmpty()) {
							for (Data d : mcrl.getData()) {
								for (String s : setData) {
									if (d.getId().equals(s))
										tmp.add(d.getRealName());
								}
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
	private String fromPathInFSMtoJsonFile(List<List<Pair<String, Set<String>>>> allpath, String nameFile)
			throws JSONException {
		List<List<Pair<String, Set<String>>>> finallist = printCounterExamplePath(allpath);
		File file = new File(dirname.getPath() + nameFile + json);
		int i = 0;
		while (file.exists())
			file = new File(dirname.getPath() + nameFile + i++ + IOTerminal.dotmcrl2);
		
		try (BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
			JSONObject ob = new JSONObject();
			for (int k = 0; k < finallist.size(); k++) {
				List<Pair<String, Set<String>>> singlepath = finallist.get(k);
				JSONObject obtask = new JSONObject();
				JSONArray arr = new JSONArray(singlepath);
				for (int j = 0; j < singlepath.size(); j++) {
					JSONObject obdata = new JSONObject();
					obdata.append("data", singlepath.get(j).getRight());
					obtask.append(singlepath.get(j).getLeft(), obdata);
				}
				ob.append("path", arr);
				output.write(ob.toString());
				if (k != finallist.size() - 1)
					output.write("\u0223");
			}
			
		} catch (IOException e) {
			new FileNotFoundException();
		}
		return file.getName();
	}

	private List<List<Pair<String, Set<String>>>> printCounterExamplePath(
			List<List<Pair<String, Set<String>>>> allpath) {
		List<List<Pair<String, Set<String>>>> finallist = new ArrayList<List<Pair<String, Set<String>>>>();
		System.out.print("PATH : ");
		for (int j = 0; j < allpath.size(); j++) {
			List<Pair<String, Set<String>>> path = allpath.get(j);
			List<Pair<String, Set<String>>> notau = new ArrayList<Pair<String, Set<String>>>();
			for (int i = 0; i < path.size(); i++) {
				if (path.get(i).getKey().equals(MCRL2.TAU.toString()))
					continue;
				notau.add(path.get(i));
			}
			if (j != allpath.size() - 1)
				System.out.print(notau.toString()+ "\u0223");
			else
				System.out.println(notau.toString());
			finallist.add(notau);
		}
		return finallist;
	}

	private long getCurrentTime() {
		return System.currentTimeMillis();
	}

	private long computeTimeSpans(long startTime) {
		return getCurrentTime() - startTime;
	}

}
