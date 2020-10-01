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
import org.apache.commons.lang3.tuple.Pair;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.bpmn.BpmnControlFlow;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.TextNode;

import algo.AbstractTranslationAlg;
import algo.CollaborativeAlg;
import algo.IDOperaion;
import formula.TextInterpreterFormula;
import io.BpmnParser;
import pcrrlalgoelement.Parout;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.Sort;
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
	private final static String evidencelts = ".pbes.evidence.lts";
	private final static String evidencefsm = ".pbes.evidence.fsm";
	private final static String dotmcrl2 = ".mcrl2";
	private final static String dotlps = ".lps";
	private final static String dotlts = ".lts";
	private final static String json = "json.json";
	private final static String dottrc = ".trc";
	private File dir = new File("result_FSAT");
	private URI dirname;
	private String mcrl2file;
	private String check;

	public IOTerminal() throws IOException {
		// If the folder result doesn't exist it generates it
		if (!dir.exists())
			dir.mkdir();
		cleanDirectory();
		dirname = dir.toURI();
		System.out.println("Insert file name");
		scan = new Scanner(System.in);
		String inputfile = scan.nextLine();
		File f = new File(inputfile);
		while (!f.exists()) {
			System.out.println("Incorrect input file, try again:");
			scan = new Scanner(System.in);
			inputfile = scan.nextLine();
			f = new File(inputfile);
		}
		String filename;
		if (f.getName().contains(".bpmn"))
			filename = f.getName().substring(0, f.getName().lastIndexOf("."));
		else
			filename = f.getName();

		while (true) {
			Set<String> datset = new HashSet<>();
			System.out.println("Select action:\n"
					+ "-> 1 Check if a <SELECTED> task has a set of <Data1,...,Datan> data \n"
					+ "-> 2 Check if a <SELECTED> partecipants has a set of  <Data1,...,Datan> data \n"
					+ "-> 3 verify (SS/AddSS/FunSS) violation \n" + "-> 4 verify if RECONSTRUCTION is ALWAYS possible\n"
					+ "-> 5 verify PK/SK encryption violaton\n" + "-> 6 exit");
			scan = new Scanner(System.in);
			String number = scan.nextLine();
			String partecipant;
			mCRL2 mcrl2;
			switch (Integer.valueOf(number)) {
			case 1:
				mcrl2 = generateSpecLps(parsebpmnfile(1, inputfile), 1, filename);
				System.out.println(mcrl2.toStringTasks() + "Choose task:");
				scan = new Scanner(System.in);
				partecipant = scan.nextLine();
				while (!mcrl2.containt(partecipant)) {
					System.out.println("INCORRECT INPUT: THIS TASK DOESN'T EXIST. CHOOSE ANOTHER ONE: ");
					scan = new Scanner(System.in);
					partecipant = scan.nextLine();
				}
				System.out.println(mcrl2.toStringData());
				datset.addAll(dataexist(mcrl2.getSortName()));
				check = TextInterpreterFormula.generateTaskFormula(mcrl2, dirname.getPath(), partecipant, datset);
				if (displayalternativeoutput(check))
					break;
				callFormula(mcrl2);
				break;
			case 2:
				mcrl2 = generateSpecLps(parsebpmnfile(2, inputfile), 2, filename);
				System.out.println(mcrl2.toStringPartecipants() + "\n Choose partecipant: \n ");
				scan = new Scanner(System.in);
				partecipant = scan.nextLine();
				while (!mcrl2.containt(partecipant)) {
					System.out.println("INCORRECT INPUT: PARTICIPANT DOESN'T EXIST. CHOOSE ANOTHER ONE: ");
					scan = new Scanner(System.in);
					partecipant = scan.nextLine();
				}
				System.out.println(mcrl2.toStringData());
				datset.addAll(dataexist(mcrl2.getSortName()));
				check = TextInterpreterFormula.generateParticipantFormula(mcrl2, dirname.getPath(), partecipant,
						datset);
				if (displayalternativeoutput(check))
					break;
				callFormula(mcrl2);
				break;
			case 3:
				mcrl2 = generateSpecLps(parsebpmnfile(3, inputfile), 3, filename);
				mcrl22lps();
				lps2lts(mcrl2);
				break;
			case 4:
				mcrl2 = generateSpecLps(parsebpmnfile(4, inputfile), 4, filename);
				Action reconstruct = mcrl2.identifyRecostructionTask();
				if (reconstruct == null) {
					System.out.println("NO RECOSTRUCTION ACTION TASK IN THE MODEL");
					break;
				}
				check = TextInterpreterFormula.generateMCFfile("<true*." + mCRL2.norecostruct + ">true",
						dirname.getPath());
				if (displayalternativeoutput(check))
					break;
				callFormula(mcrl2);
				break;
			case 5:
				mcrl2 = generateSpecLps(parsebpmnfile(5, inputfile), 5, filename);
				mcrl22lps();
				lps2lts(mcrl2);
				break;
			case 6:
				cleanDirectory();
				System.exit(0);
			default:
				System.out.println("OPERATION NOT RECOGNIZED");
				break;
			}
			continueOrExit();
		}
	}

	// Clean the current directory from all the file already there
	private void cleanDirectory() {
		for (File file : dir.listFiles()) {
			if (!file.isDirectory())
				file.delete();
		}
	}

	private CollaborativeAlg parsebpmnfile(int i, String inputfile) {
		Pair<Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>, Set<Pair<FlowNode, FlowNode>>> set = null;
		try {
			set = BpmnParser.collaborationParser(inputfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new CollaborativeAlg(set);

	}

	private Set<String> dataexist(Sort sort) {
		boolean dontexist = false;
		Set<String> datset = new HashSet<String>();
		datset.addAll(scanData());
		while (!dontexist) {
			for (String d : datset) {
				if (sort.containtType(d))
					dontexist = true;
				else {
					System.out.println(d + " IS NOT A DATA TYPE IN THE SPECIFICATION");
					dontexist = false;
					datset.clear();
					datset.addAll(scanData());
					break;
				}
			}
		}
		return datset;
	}

	private mCRL2 generateSpecLps(CollaborativeAlg col, int id_op, String filename) {
		// long startTime = getCurrentTime();
		mCRL2 mcrl2 = col.getSpec();
		Parout parout = new Parout();
		mcrl2 = parout.parout(mcrl2);
		mcrl2file = mcrl2.toFile(dirname.getPath() + filename);
		// long endTime = getCurrentTime();
		// System.out.println("Traduction time: " + computeTimeSpanms(startTime,
		// endTime) + " ms");
		String lpsgen = "mcrl22lps " + mcrl2file + dotmcrl2 + " " + mcrl2file + dotlps;
		runmcrlcommand(lpsgen);
		return mcrl2;
	}

	private void callFormula(mCRL2 mcrl2) {
		// long startTime = getCurrentTime();
		boolean resultbool = lps2pbes2solve2convert();
		System.out.println(resultbool);
		try {
			if (resultbool && (AbstractTranslationAlg.id_op != IDOperaion.RECONSTRUCTION.getVal())) {
				if (new File(dirname.getPath() + mcrl2file + evidencefsm).exists()) {
					List<Pair<String, Set<String>>> s = scanFSMfile(dirname.getPath() + mcrl2file + evidencefsm, mcrl2);
					fromPathInFSMtoJsonFile(dirname.getPath() + mcrl2file + json, s);
				}
			} else if(resultbool && (AbstractTranslationAlg.id_op == IDOperaion.RECONSTRUCTION.getVal())) {
				lps2lts(mcrl2);
			}else
				System.out.println("No JSON file generated because there isn't a path to show");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// long endTime = getCurrentTime();
		// System.out.println("Verification time: " + computeTimeSpans(startTime,
		// endTime) + " s");
	}

	// return true if an unexpected output exist, false otherwise
	private boolean displayalternativeoutput(String s) {
		if (s == null)
			System.out.println("This task/partecipant doesn't exist");
		else if (s.equals("-1"))
			System.out.println("NEVER HAS THIS NUMBER OF PARAMETERS");
		else
			return false;
		return true;
	}

	private void mcrl22lps() {
		String mcrl22lps = "mcrl22lps -lstack " + mcrl2file + dotmcrl2 + " " + mcrl2file + dotlps;
		runmcrlcommand(mcrl22lps);
	}

	private void lps2lts( mCRL2 mcrl2) {
		String lps2lts = "";
		if (AbstractTranslationAlg.id_op == IDOperaion.SSSHARING.getVal() || AbstractTranslationAlg.id_op == IDOperaion.ENCRYPTION.getVal())
			lps2lts = "lps2lts -a" + mCRL2.violation + " -t1 " + mcrl2file + dotlps;
		else if(AbstractTranslationAlg.id_op == IDOperaion.RECONSTRUCTION.getVal())
			lps2lts = "lps2lts -a" + mCRL2.recostruct + " -t1 " + mcrl2file + dotlps;
		runmcrlcommand(lps2lts);
		List<String> path = new ArrayList<String>();
		;
		File dir = new File(dirname);
		String[] children = dir.list();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				// Get filename of file or directory
				String filename = children[i];
				File file = new File(dirname + File.separator + filename);
				if (!file.isDirectory() && file.getName().endsWith(dottrc)) {
					String tracepp = "tracepp " + filename + " " + filename.replace(dottrc, dotmcrl2);
					runmcrlcommand(tracepp);
					FileReader fileReader;
					try {
						fileReader = new FileReader(dirname.getPath() + filename.replace(dottrc, dotmcrl2));
						BufferedReader br = new BufferedReader(fileReader);
						String line;
						while ((line = br.readLine()) != null) {
							if (!line.equals("tau"))
								path.add(line);
						}
						br.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
			}
		}
		if (path.isEmpty()) {
			if (AbstractTranslationAlg.id_op == IDOperaion.SSSHARING.getVal())
				System.out.println(IDOperaion.SSSHARING + " IS PRESERVED");
			else if (AbstractTranslationAlg.id_op == IDOperaion.ENCRYPTION.getVal()) {
				System.out.println(IDOperaion.ENCRYPTION + " IS PRESERVED");
			} else if (AbstractTranslationAlg.id_op == IDOperaion.RECONSTRUCTION.getVal())
				System.out.println("NOT RECOSTRUCTED");
		} else {
			// System.out.println(path);
			List<Pair<String, Set<String>>> list = generateList(path, mcrl2);
			try {
				fromPathInFSMtoJsonFile(dirname.getPath() + mcrl2file + json, list);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private List<Pair<String, Set<String>>> generateList(List<String> path, mCRL2 mcrl2) {
		List<Pair<String, Set<String>>> list = new ArrayList<Pair<String, Set<String>>>();
		for (int i = 0; i < path.size(); i++) {
			String el = path.get(i);
			if (el.equals(mCRL2.violation) || el.equals(mCRL2.recostruct) || el.equals(mCRL2.norecostruct))
				continue;
			String nametask = el.substring(0, el.indexOf("("));
			Set<String> setdata = new HashSet<String>();
			for (String s : mcrl2.getSortName().getTypes()) {
				if (el.contains(Utils.adjustName(s)))
					setdata.add(s);
			}

			list.add(Pair.of(nametask, setdata));
		}
		return list;
	}

	private boolean lps2pbes2solve2convert() {
		String lps2lts = "lps2lts " + mcrl2file + dotlps + " " + mcrl2file + dotlts;
		runmcrlcommand(lps2lts);
		String ltsconvert = "ltsconvert -etau-star " + mcrl2file + dotlts + " " + mcrl2file + dotlts;
		runmcrlcommand(ltsconvert);
		String lts2pbes = "lts2pbes -c -f " + check + " " + mcrl2file + dotlts + " " + mcrl2file + ".pbes";
		runmcrlcommand(lts2pbes);
		String pbessolve = "pbessolve -s1 --file=" + mcrl2file + dotlts + " " + mcrl2file + ".pbes";
		String resultsolve = runmcrlcommand(pbessolve);

		if (AbstractTranslationAlg.id_op != IDOperaion.RECONSTRUCTION.getVal()) {
			if (resultsolve.equals("false"))
				return false;
			String ltsconvert2 = "ltsconvert -eweak-trace " + mcrl2file + evidencelts + " " + mcrl2file + evidencefsm;
			runmcrlcommand(ltsconvert2);
			return true;

		} else {
			if (resultsolve.equals("true")) {
				return false;
			}else {
				return true;
			}
		}
	}

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
		for (int i = 0; i < split.length; i++) {
			String dataname = split[i].replace(" ", "_");
			datset.add(dataname);
		}
		return datset;
	}

	private void continueOrExit() {
		System.out.println("CONTINUE (Y/N) : ");
		String r = scan.nextLine();
		while (!r.equalsIgnoreCase(NO) && !r.equalsIgnoreCase(YES)) {
			System.out.println("command not available, try again ... ");
			r = scan.nextLine();
		}
		cleanDirectory();
		if (r.equalsIgnoreCase(YES)) {
			return;
		} else
			System.exit(0);

	}

	public List<Pair<String, Set<String>>> scanFSMfile(String fileName, mCRL2 mcrl) {
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
					// Pattern pattern = Pattern.compile("node\\(.*\\)");
					TaskProcess t;
					if ((t = TextInterpreterFormula.identifyIdTaskFormula(mcrl, task)) != null) {
						Set<String> tmp = new HashSet<String>();
						for (String s : mcrl.getSortName().getTypes()) {
							if (line.contains("node(" + s + ")"))
								tmp.add(s);
						}
						/*
						 * Matcher m = pattern.matcher(line); while (m.find()) { String[] match =
						 * m.group(0).replace("(", "").replace(")", "").replace("{",
						 * "").replace("}","").replace("node","").trim().split(","); for (int i = 0; i <
						 * match.length; i++) { if (!match[i].isEmpty()) tmp.add(match[i].trim()); } }
						 */
						map.put(Pair.of(Integer.valueOf(split[0]), Integer.valueOf(split[1])),
								Pair.of(t.getAction().getId(), tmp));
					}
				}
			}
			br.close();
			return recognizePath(map, root);
		} catch (IOException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
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

	private void fromPathInFSMtoJsonFile(String pathfile, List<Pair<String, Set<String>>> path) throws JSONException {
		System.out.println("PATH : " + path.toString());
		File file = new File(pathfile);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private long getCurrentTime() {
		return System.nanoTime();
	}

	private long computeTimeSpans(long startTime, long endTime) {
		return TimeUnit.NANOSECONDS.toSeconds(endTime - startTime);
	}

}
