package spec.mcrl2obj;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.io.FilenameUtils;

import java.util.Set;
import com.google.common.collect.Sets;

import io.DotBPMNKeyW;
import io.pet.PETLabel;
import rpstTest.IOTerminal;
import rpstTest.Utils;
import sort.Collection;
import sort.Data;
import sort.ISort;
import sort.Memory;
import sort.Name;
import sort.Placeholder;
import sort.Privacy;
import spec.ISpec;
import spec.mcrl2obj.Processes.Buffer;
import spec.mcrl2obj.Processes.ParticipantProcess;
import spec.mcrl2obj.Processes.TaskProcess;

/**
 * This is the MCRL2 class that defines an object describing a mCRL2
 * specification
 * 
 * @author S. Belluccini
 *
 */
public class MCRL2 implements ISpec {

	private static final String DATA_NAME = "d";
	private static final String INPUT = "i";
	private static final String OUTPUT = "o";
	private static final String SENDREAD = "sr";
	private static final String TEMPORARY = "t";
	public static final Action DELTA = new Action("delta","delta");
	public static final Action TAU = new Action("tau", "tau");
	public static final Action VIOLATION = new Action("VIOLATION", new Placeholder("", Memory.nameSort()));
	public static final Action NOVIOLATION = new Action("NOVIOLATION");
	public static final Action CONTAIN = new Action("CONTAIN");
	private static int DATA_NAME_INDEX = 0;
	private static int INPUT_INDEX = 0;
	private static int OUTPUT_INDEX = 0;
	private static int TEMPORARY_INDEX = 0;
	private Set<String> map;
	private Set<PETLabel> petLabel;
	private Set<Placeholder> var;
	private Set<String> eqn;
	private Set<String> dataObjectName;
	private Set<ISort> isort;
	private Set<Action> actions;
	private Set<Action> allow;
	private Set<CommunicationFunction> comm;
	private Set<Action> hide;
	private Set<Buffer> processes;
	private Set<ParticipantProcess> particiantProcess;
	private Placeholder m1 = new Placeholder(Memory.nameSort());
	private Placeholder m2 = new Placeholder(Memory.nameSort());
	private Placeholder d = new Placeholder(Data.nameSort());

	public MCRL2() {
		this.map = Sets.newHashSet(
				MCRL2Utils.printMap(MCRL2Utils.unionf, Memory.nameSort(), Memory.nameSort(), Memory.nameSort()),
				MCRL2Utils.printMap(MCRL2Utils.emptyf, ISort.BOOL.toString(), Data.nameSort()));
		this.var = Sets.newHashSet(m1, m2, d);
		this.eqn = Sets.newHashSet(MCRL2Utils.printifeqn(MCRL2Utils.printH(m2.toString()) + " in " + m1.toString(),
				MCRL2Utils.printf(MCRL2Utils.unionf, m1.toString(), m2.toString()),
				MCRL2Utils.printf(MCRL2Utils.unionf, m1.toString(), MCRL2Utils.printT(m2.toString()))), MCRL2Utils.printifeqn("!("+MCRL2Utils.printH(m2.toString()) + " in " + m1.toString()+")",
						MCRL2Utils.printf(MCRL2Utils.unionf, m1.toString(), m2.toString()),
						MCRL2Utils.printf(MCRL2Utils.unionf, m1.toString()+"<|"+MCRL2Utils.printH(m2.toString()), MCRL2Utils.printT(m2.toString()))),
				MCRL2Utils.printifeqn(m2.toString() + "== []", MCRL2Utils.printf(MCRL2Utils.unionf, m1.toString(), m2.toString()), m1.toString()),
				MCRL2Utils.printifeqn(m1.toString() + "== []", MCRL2Utils.printf(MCRL2Utils.unionf, m1.toString(), m2.toString()), m2.toString()),
				MCRL2Utils.printifeqn(d.toString() + "==" + Data.eps().toString(), MCRL2Utils.printf(MCRL2Utils.emptyf, d.toString()), ISort.TRUE.toString()),
				MCRL2Utils.printifeqn(d.toString() + "!=" + Data.eps().toString(), MCRL2Utils.printf(MCRL2Utils.emptyf, d.toString()), ISort.FALSE.toString()));
		this.dataObjectName = new HashSet<String>();
		this.actions = new HashSet<Action>();
		this.allow = new HashSet<Action>();
		this.comm = new HashSet<CommunicationFunction>();
		this.hide = new HashSet<Action>();
		this.processes = new HashSet<Buffer>();
		this.particiantProcess = new HashSet<ParticipantProcess>();
		this.petLabel = new HashSet<PETLabel>();
		this.isort = Sets.newHashSet(new Collection(), new Data(), new Memory(), new Name(), new Privacy());
	}

	/**
	 * Returns an input action (i) with parameters
	 * 
	 * @param parameters of the action
	 * @return an input action (i) with parameters
	 */
	public static Action getInputAction(ISort... parameters) {
		return new Action(INPUT + INPUT_INDEX++, parameters);
	}

	/**
	 * Returns an output action (o) with parameters
	 * 
	 * @param parameters of the action
	 * @return an output action (o) with parameters
	 */
	public static Action getOutputAction(ISort... parameters) {
		return new Action(OUTPUT + OUTPUT_INDEX++, parameters);
	}

	/**
	 * Returns the new name for a data or placeholder
	 * 
	 * @return the new name of a data or placeholder
	 */
	public static String getDataName() {
		return DATA_NAME + DATA_NAME_INDEX++;
	}
	
	/**
	 * Returns the name for the result of a synchronization action
	 * @return the name for the result of a synchronization action
	 */
	public static String getComFunResult() {
		return SENDREAD + TEMPORARY_INDEX++;
	}

	/**
	 * Returns a new temporary action
	 * 
	 * @return a new temporary action
	 */
	public static Action getTemporaryAction(ISort... data) {
		return new Action(TEMPORARY + TEMPORARY_INDEX++, data);
	}

	/**
	 * Returns the task process associated to the action a if exists
	 * 
	 * @param s name of the action
	 * @return the task process associated to the action a if exists, otherwise null
	 */
	public TaskProcess getActionTask(String s) {
		for (TaskProcess t : getTaskProcesses()) {
			if (t.getAction().getId().equals(s))
				return t;
		}
		return null;
	}

	public Set<String> getDataObjectName() {
		return this.dataObjectName;
	}

	public Set<Data> getData() {
		Set<Data> data = new HashSet<Data>();
		for (Action a : actions) {
			if (a.getClass().equals(TaskAction.class)) {
				for (ISort d : ((TaskAction) a).getParameters()) {
					if (d.getClass().equals(Data.class))
						data.add((Data) d);
				}
			}
		}
		return data;
	}


	
	public void addCommunicaitonFunction(CommunicationFunction f) {
		this.comm.add(f);
	}

	public void addCommunicationFunction(Set<CommunicationFunction> fuctions) {
		this.comm.addAll(fuctions);
	}
	
	public void removeCommunicationFunction(Set<CommunicationFunction> functions) {
		this.comm.removeAll(functions);
	}

	/**
	 * Adds actions to the set of actions, it also update the set of PETLabel
	 * 
	 * @param actions the set of actions to be added
	 */
	public void addAction(Action... actions) {
		for (int i = 0; i < actions.length; i++) {
			this.actions.add(actions[i]);
		}
	}
	
	public void removeActoin(Set<Action> actions) {
		this.actions.removeAll(actions);
	}

	public void addAllow(Action... a) {
		for (int i = 0; i < a.length; i++) {
			this.allow.add(a[i]);
		}

	}
	
	public void removeAllow(Set<Action> allows) {
		this.allow.removeAll(allows);
	}

	public void addHide(Action... h) {
		for (Action a : h) {
			if (!this.hide.contains(a))
				this.hide.add(a);
		}
	}
	
	public void removeHide(Set<Action> hides) {
		this.hide.removeAll(hides);
	}

	/**
	 * Adds the elements we sort data to the data set
	 * 
	 * @param sorts the elemenets to insert
	 */
	public void addDataObjectName(String s) {
		this.dataObjectName.add(s);
	}

	public void addMap(String... s) {
		if(s.length != 0) {
			for(String map : s)
				this.map.add(map);
		}
	}
	
	public void removeMap(Set<String> strings) {
		this.map.removeAll(strings);
	}

	public void addVar(Placeholder... p) {
		for (Placeholder placehoder : p)
			this.var.add(placehoder);
	}

	public void removeVar(Set<Placeholder> vars) {
		this.var.removeAll(vars);
	}
	public void addEqn(String... eqn) {
		if(eqn.length!= 0) {
			for(String e : eqn)
				this.eqn.add(e);
		}
	}
	
	public void removeEqn(Set<String> strings) {
		this.eqn.removeAll(strings);
	}

	/*
	 * public Set<Data> getData() { return this.data; }
	 */

	/**
	 * Returns all the tasks processes of all the parties in the collaboration
	 * 
	 * @return all the tasks processes of all the parties in the collaboration
	 */
	public Set<TaskProcess> getTaskProcesses() {
		Set<TaskProcess> sets = new HashSet<TaskProcess>();
		for (ParticipantProcess p : particiantProcess)
			sets.addAll(p.getTaskProcesses());
		return sets;
	}

	/**
	 * Returns the participant with the given name if exists, otherwise null
	 * 
	 * @param nameparty of the participant
	 * @return the participant with that name if exists, otherwise null
	 */
	public ParticipantProcess getParticipant(String nameparty) {
		for (ParticipantProcess p : getParcipantProcesses()) {
			if (p.getId().equals(nameparty))
				return p;
		}
		return null;
	}

	/**
	 * Returns the set of Participant Processes in this mcrl2 object
	 * 
	 * @return
	 */
	public Set<ParticipantProcess> getParcipantProcesses() {
		return this.particiantProcess;
	}

	/**
	 * Adds the label to the set of petlabel
	 * @param label the label of the pet
	 */
	public void addPetLabel(PETLabel label) {
		this.petLabel.add(label);
	}
	/**
	 * Returns all the petlabel associated to the processes in this specification
	 * @return all the petlabel associated to the processes in this specification
	 */
	public Set<PETLabel> getPetLabel() {
		for(TaskProcess t : getTaskProcesses()) {
			if(t.hasPET())
				this.petLabel.add(t.getPETLabel());
		}
		return this.petLabel;
	}

	public void addProcess(Buffer process) {
		this.processes.add(process);
	}

	public void addParticipantProcess(ParticipantProcess p) {
		this.particiantProcess.add(p);
		for (TaskProcess t : p.getTaskProcesses())
			this.actions.add(t.getAction());
	}
	
	public Set<String> getEndEvents(){
		Set<String> endevents = new HashSet<String>();
		for(TaskProcess t : getTaskProcesses()) {
			if(t.getBPMNElement()!= null && t.getBPMNElement().equals(DotBPMNKeyW.ENDEVENT))
				endevents.add(t.getAction().getId());
		}
		return endevents;
	}

	/**
	 * Checks if one of the participants has this task among their processes by compering their id.
	 * 
	 * @param idTask the id of the task
	 * @return true if one of the participants has this task among their processes,
	 *         false otherwise
	 */
	public boolean containsTask(String idTask) {
		for (ParticipantProcess p : getParcipantProcesses()) {
			if (p.containActionTask(idTask))
				return true;
		}
		return false;
	}

	public boolean containsParticipant(String party) {
		for (ParticipantProcess p : getParcipantProcesses()) {
			if (p.getId().equals(party))
				return true;
		}
		return false;
	}

	/**
	 * Returns the sort section of the mCRL2 specification
	 * 
	 * @return the sort section of the mCRL2 specification
	 */
	private String printSorts() {
		if (!this.isort.isEmpty()) {
			String s = MCRL2Utils.sort + "\n";
			for (ISort d : isort) {
				s += d.toStringSort() + "\n";
			}
			return s;
		}
		return "";
	}

	/**
	 * Returns the map section of the mCRL2 specification
	 * 
	 * @return the map section of the mCRL2 specification
	 */
	private String printMaps() {
		if (!this.map.isEmpty()) {
			String result = MCRL2Utils.map + "\n";
			for (String s : this.map) {
				result += s;
			}
			return result;
		} else
			return "";
	}

	/**
	 * Returns the var section of the mCRL2 specification
	 * 
	 * @return the var section of the mCRL2 specification
	 */
	private String printVar() {
		if (!this.var.isEmpty()) {
			String result = MCRL2Utils.var + "\n";
			for (Placeholder p : this.var) {
				result += p.toStringVar();
			}
			return result;
		} else
			return "";
	}

	/**
	 * Returns the eqn section of the mCRL2 specification
	 * 
	 * @return the eqn section of the mCRL2 specification
	 */
	private String printEqn() {
		if (!this.eqn.isEmpty()) {
			String result = MCRL2Utils.eqn + "\n";
			for (String s : this.eqn) {
				result += s;
			}
			return result;
		} else
			return "";
	}

	/**
	 * Returns the act section of the mCRL2 specification
	 * 
	 * @return the act section of the mCRL2 specification
	 */
	private String printAct() {
		if (!this.actions.isEmpty()) {
			String result = MCRL2Utils.act + "\n";
			Map<String, Set<String>> classact = classifyaction();
			for (Entry<String, Set<String>> entry : classact.entrySet()) {
				int i = 0;
				for (String a : entry.getValue()) {
					result = result + a;
					if (i != entry.getValue().size() - 1)
						result = result + ",";
					i++;
				}
				if (entry.getKey().equals(""))
					result = result + ";\n";
				else
					result = result + ": " + entry.getKey() + ";\n";
			}
			return result;
		} else
			return "";
	}

	/**
	 * Returns the complete mCRL2 specification to be printed
	 */
	@Override
	public String toString() {
		String s = printSorts();
		s = s + printMaps();
		s = s + printVar();
		s = s + printEqn();
		s = s + printAct();
		// It prints the proc section of the mCRL2 specification
		s = s + "proc" + "\n";
		String init = "";
		for (Buffer process : processes) {
			s = s + process.toString();
			if (!init.isEmpty())
				init += "||";
			init += process.inizializeBuffer();
		}
		for (ParticipantProcess p : particiantProcess) {
			s = s + p.toString();
			if (!init.isEmpty())
				init += "||";
			init += p.getId();
			if(!p.initializeMemoryCallP().isEmpty()) {
				init += "||" + p.initializeMemoryCallP();
			}
		}
		s = s + "init ";
		int par = 0;
		if (!hide.isEmpty()) {
			s = s + "hide ({";
			int i = 0;
			for (Action a : hide) {
				s = s + a.getId();
				if (i != hide.size() - 1)
					s = s + ",";
				i++;
			}
			s = s + "},";
			par++;
		}
		if (!allow.isEmpty()) {
			s = s + "allow ({";
			int i = 0;
			for (Action a : allow) {
				s = s + a.getId();
				if (i != allow.size() - 1)
					s = s + ",";
				i++;
			}
			s = s + "},";
			par++;
		}
		if (!comm.isEmpty()) {
			s = s + "comm ({";
			int i = 0;
			for (CommunicationFunction a : comm) {
				s = s + a.toString();
				if (i != comm.size() - 1)
					s = s + ",";
				i++;
			}
			s = s + "},";
			par++;
		}
		s += init;
		while (par != 0) {
			s = s + ")";
			par--;
		}
		return s + ";";
	}

	/**
	 * Returns a map <Type, Set<Action>
	 * 
	 * @return a map <Type, Set<Action>
	 */
	private Map<String, Set<String>> classifyaction() {
		// n of parameters - action
		Map<String, Set<String>> classact = new HashMap<String, Set<String>>();
		for (Action a : actions) {
			String type = a.printActionType();
			if (classact.containsKey(type))
				classact.get(type).add(a.getId());
			else {
				Set<String> actions = Sets.newHashSet(a.getId());
				classact.put(type, actions);
			}
		}
		return classact;
	}

	/*
	 * public void taureduction() { List<String> processtoremove = new
	 * ArrayList<String>(); for (AbstractProcess p : processes) { if
	 * ((p.getClass().equals(Process.class) && ((Process) p))) ||
	 * p.getClass().equals(ParticipantProcess.class)) {
	 * 
	 * Process process; if (p.getClass().equals(ParticipantProcess.class)) process =
	 * ((ParticipantProcess) p).getProcess(); else process = (Process) p;
	 * 
	 * List<String> newchilds = new ArrayList<String>(); for (int i = 0; i <
	 * process.getLength(); i++) { AbstractProcess child =
	 * identifyAbstractProcess(process.getChildName(i)); if (child == null) {
	 * newchilds.add(process.getChildName(i)); continue; } if
	 * (child.getClass().equals(Process.class)) { if (((Process) child).isActivity()
	 * && ((Process) child).getAction().isTau()) {
	 * processtoremove.add(process.getChildName(i)); } else
	 * newchilds.add(process.getChildName(i)); } else
	 * newchilds.add(process.getChildName(i)); } process.setChild(newchilds); }
	 * 
	 * } Set<AbstractProcess> newprocess = new HashSet<AbstractProcess>(); for
	 * (AbstractProcess p : processes) { if (!processtoremove.contains(p.getName()))
	 * { newprocess.add(p);
	 * 
	 * } } this.processes = newprocess; emptyprocess(); }
	 */

	/*
	 * private void emptyprocess() { boolean change = true; while (change) { change
	 * = false; for (AbstractProcess p : processes) {
	 * 
	 * if (p.getClass().equals(Process.class) ||
	 * p.getClass().equals(ParticipantProcess.class)) { Process process; if
	 * (p.getClass().equals(Process.class)) process = (Process) p; else process =
	 * ((ParticipantProcess) p).getProcess(); // Empty process if
	 * (process.getAction() == null && !process.hasChild()) {
	 * this.processes.remove(p); change = true; break; } else if
	 * (process.hasChild()) { // Process thatcontainedemptyprocess or single
	 * references to process are removed List<String> newchild = new
	 * ArrayList<String>(); boolean removec = false; for (int i = 0; i <
	 * process.getLength(); i++) { if
	 * (identifyAbstractProcess(process.getChildName(i)) != null ||
	 * process.getInsideDef(process.getChildName(i)) != null)
	 * newchild.add(process.getChildName(i)); else removec = true; } if (removec) {
	 * process.setChild(newchild); change = true; break; }
	 * 
	 * } }
	 * 
	 * } } }
	 */

	/*
	 * public TaskProcess identifyTaskProcessFromAction(Action a) { for
	 * (AbstractProcess p : processes) { if (p.getClass().equals(TaskProcess.class)
	 * && ((TaskProcess) p).getAction().equals(a)) return (TaskProcess) p; } return
	 * null; }
	 */

	/*
	 * public AbstractProcess identifyAbstractProcess(String name) { for
	 * (AbstractProcess p : processes) { if (p.getId().equals(name)) return p; }
	 * return null; }
	 */

	/*
	 * return true: if exist a process that represent a task or a partecipant with
	 * this name
	 */
	/*
	 * public boolean containt(String s) { for (AbstractProcess ab : processes) { if
	 * ((ab.getClass().equals(TaskProcess.class) && ((TaskProcess) ab).getAction()
	 * != null && ((TaskProcess) ab).getAction().getId().equals(s)) ||
	 * (ab.getClass().equals(ParticipantProcess.class) && ((ParticipantProcess)
	 * ab).getId().equals(s))) return true; } return false; }
	 */

	/*
	 * public void removeProcess(String name) { for (AbstractProcess ap : processes)
	 * { if (ap.getId().equals(name)) { processes.remove(ap); break; } }
	 * 
	 * }
	 */

	/**
	 * Returns a string describing all the parties in the specification using their
	 * id and name
	 * 
	 * @return a string describing all the parties in the specification using their
	 *         id and name
	 */
	public String toStringPartecipants() {
		String s = "PARTECIPANTS:\n";
		for (ParticipantProcess ap : particiantProcess) {
				s = s + " ID: " + ap.getId() + " NAME: " + ap.getName()
						+ "\n";
		}
		return s;
	}

	/*
	 * public void removePinInitSet(String name) { this.initSet.remove(name); }
	 */
	/**
	 * Returns a string describing all the tasks in the specification usin their id
	 * and name
	 * 
	 * @return a string describing all the tasks in the specification usin their id
	 *         and name
	 */
	public String toStringTasks() {
		String s = "All tasks in the model:\n";
		for (TaskProcess t : getTaskProcesses()) {
			TaskAction a = t.getAction();
			s = s + "ID: " + a.getId();
			if (a.getRealName()!= null && !a.getRealName().isEmpty())
				s = s + " Name: " + a.getRealName();
			s += "\n";
		}
		return s;
	}

	/**
	 * Returns a string describing all the data in the specification using their
	 * name
	 * 
	 * @return a string describing all the data in the specification using their
	 *         name
	 */
	public String toStringData() {
		String string = "All data object in the model: \n";
		for (String s : dataObjectName) {
			string += s + "\n";
		}
		return string;
	}

	public String toFile(String fileName) {
		File file = new File(fileName + IOTerminal.dotmcrl2);
		while (file.exists())
			file = new File(fileName + (Utils.getId()) + IOTerminal.dotmcrl2);

		try (BufferedWriter output = new BufferedWriter(new FileWriter(file))) {
			output.write(toString());
			output.close();
		} catch (IOException e) {
			e = new IOException("Fail to generate" + file);
			System.err.println(e.toString());
		}
		return FilenameUtils.getBaseName(file.getName());
	}

	/*
	 * private String printvar(String domain, String... vars) { String s = ""; for
	 * (int i = 0; i < vars.length; i++) { s = s + vars[i]; if (i != vars.length -
	 * 1) s = s + ","; } s = s + " : " + domain + ";\n"; return s; }
	 */

	// Print in output of all the function in map, var and eqn, remember to modify 1
	// with the real thresold value
	/*
	 * private String memoryToString() { String m1 =
	 * Action.setTemporaryAction().getName(); String m2 =
	 * Action.setTemporaryAction().getName(); String pn =
	 * Action.setTemporaryAction().getName(); String n =
	 * Action.setTemporaryAction().getName(); String p =
	 * Action.setTemporaryAction().getName(); String data =
	 * Action.setTemporaryAction().getName(); String i =
	 * Action.setTemporaryAction().getName(); String id =
	 * Action.setTemporaryAction().getName(); String b =
	 * Action.setTemporaryAction().getName(); String s = "map \r\n"; s = s +
	 * printMap("union", sortmemory, sortmemory, sortmemory);
	 * 
	 * s = s + printMap("empty", sortbool, sortdata); if
	 * (AbstractTranslationAlg.id_op == IDOperaion.SSSHARING.getVal()) { s = s + TH
	 * + " : " + sortnat.getName() + ";\n"; s = s + printMap("sssharingviolation",
	 * sortbool, sortnat, sortmemory); s = s + printMap("sslist", sortnat, sortnat,
	 * sortmemory, sortnat); s = s + printMap("sscompviolation", sortbool, sortnat,
	 * sortmemory); s = s + printMap("sscomp", sortnat, sortnat, sortmemory,
	 * sortnat); s = s + printMap("ssrecviolation", sortbool, sortmemory); } else if
	 * (AbstractTranslationAlg.id_op == IDOperaion.ENCRYPTION.getVal()) { s = s +
	 * printMap("haskey", sortbool, sortmemory, sortnat); s = s +
	 * printMap("hascipher", sortbool, sortmemory, sortnat); s = s +
	 * printMap("encryptionviolation", sortbool, sortmemory, sortnat); } else if
	 * (AbstractTranslationAlg.id_op == IDOperaion.RECONSTRUCTION.getVal()) { s = s
	 * + TH + " : " + sortnat.getName() + ";\n"; // s = s + printMap("rlist",
	 * sortnat, sortmemory, sortnat); // s = s + printMap("is_recostructed",
	 * sortbool, sortmemory); s = s + printMap("list2bag", sortbagnat, sortmemory,
	 * sortbagnat); s = s + printMap("is_recostructed", sortbool, sortbagnat); } s =
	 * s + "var\r\n"; s = s + printvar(sortmemory, m1, m2); s = s +
	 * printvar(sortdata, data); if (AbstractTranslationAlg.id_op ==
	 * IDOperaion.SSSHARING.getVal() || AbstractTranslationAlg.id_op ==
	 * IDOperaion.RECONSTRUCTION.getVal() || AbstractTranslationAlg.id_op ==
	 * IDOperaion.ENCRYPTION.getVal()) { s = s + printvar(sortpname, pn); s = s +
	 * printvar(sortname, n); s = s + printvar(sortprivacy, p); s = s +
	 * printvar(sortnat, i, id); s = s + printvar(sortbagnat, b); } s = s +
	 * "eqn \n"; if (AbstractTranslationAlg.id_op == IDOperaion.TASK.getVal() ||
	 * AbstractTranslationAlg.id_op == IDOperaion.PARTICIPANT.getVal()) { s = s +
	 * printf(unionf, m1, m2) + "=" + m1 + " + " + m2 + ";\n"; s = s +
	 * printifeqn(data + "== " + eps, printtruef(printf(emptyf, data)), ""); s = s +
	 * printifeqn(data + "!= " + eps, printfalsef(printf(emptyf, data)), ""); // s =
	 * s + printf(emptyf, m1) + "= { dd :" + sortdata.getName() + " | ({ dd } * //
	 * " + m1 + "!= {}) && (dd != " // + eps + ")} == {};\n"; } else if
	 * (AbstractTranslationAlg.id_op == IDOperaion.SSSHARING.getVal() ||
	 * AbstractTranslationAlg.id_op == IDOperaion.RECONSTRUCTION.getVal() ||
	 * AbstractTranslationAlg.id_op == IDOperaion.ENCRYPTION.getVal()) { s = s +
	 * printifeqn(printf(head, m2) + " in " + m1, printf(unionf, m1, m2),
	 * printf(unionf, m1, printf(tail, m2))); s = s + printifeqn("!(" + printf(head,
	 * m2) + " in " + m1 + ")", printf(unionf, m1, m2), printf(unionf, m1 + " <| " +
	 * printf(head, m2), printf(tail, m2))); s = s + printifeqn(m2 + "== [] ",
	 * printf(unionf, m1, m2), m1); s = s + printifeqn(m1 + "== [] ", printf(unionf,
	 * m1, m2), m2); s = s + printifeqn(data + "== " + eps,
	 * printtruef(printf(emptyf, data)), ""); s = s + printifeqn(data + "!= " + eps,
	 * printfalsef(printf(emptyf, data)), ""); } if (AbstractTranslationAlg.id_op ==
	 * IDOperaion.SSSHARING.getVal() || AbstractTranslationAlg.id_op ==
	 * IDOperaion.RECONSTRUCTION.getVal()) { s = s + TH + "=" + getValueTreshold() +
	 * ";\n"; // if its just sssharing checking if (AbstractTranslationAlg.id_op ==
	 * IDOperaion.SSSHARING.getVal()) { s = s + printifeqn(printf("sslist", id, m1,
	 * String.valueOf(0)) + ">=" + TH, printtruef(printf("sssharingviolation", id,
	 * m1)), ""); s = s + printifeqn(printf("sslist", id, m1, String.valueOf(0)) +
	 * "<" + TH, printfalsef(printf("sssharingviolation", id, m1)), ""); s = s +
	 * printifeqn( printf(is_pn, printf(head, m1)) + " && " + printf(is_sss,
	 * printf(frt, printf(pv, printf(head, m1)))) + " && " + printf(snd, printf(pv,
	 * printf(head, m1))) + " == " + id, printf("sslist", id, m1, i),
	 * printf("sslist", id, printf(tail, m1), String.valueOf(i) + "+" + 1)); s = s +
	 * printifeqn( printf("!(" + is_pn, printf(head, m1)) + ") || !(" +
	 * printf(is_sss, printf(frt, printf(pv, printf(head, m1)))) + ") || " +
	 * printf(snd, printf(pv, printf(head, m1))) + " != " + id, printf("sslist", id,
	 * m1, i), printf("sslist", id, printf(tail, m1), i)); s = s + printifeqn(m1 +
	 * "== []", printf("sslist", id, m1, i), i); s = s + printifeqn(printf("sscomp",
	 * id, m1, String.valueOf(0)) + ">=" + TH, printtruef(printf("sscompviolation",
	 * id, m1)), ""); s = s + printifeqn(printf("sscomp", id, m1, String.valueOf(0))
	 * + "<" + TH, printfalsef(printf("sscompviolation", id, m1)), ""); s = s +
	 * printifeqn( printf(is_pn, printf(head, m1)) + " && " + printf(is_ssc,
	 * printf(frt, printf(pv, printf(head, m1)))) + " && " + printf(snd, printf(pv,
	 * printf(head, m1))) + " == " + id, printf("sscomp", id, m1, i),
	 * printf("sscomp", id, printf(tail, m1), String.valueOf(i) + "+" + 1)); s = s +
	 * printifeqn( printf("!(" + is_pn, printf(head, m1)) + ") || !(" +
	 * printf(is_ssc, printf(frt, printf(pv, printf(head, m1)))) + ") || " +
	 * printf(snd, printf(pv, printf(head, m1))) + " != " + id, printf("sscomp", id,
	 * m1, i), printf("sscomp", id, printf(tail, m1), i)); s = s + printifeqn(m1 +
	 * "== []", printf("sscomp", id, m1, i), i); s = s + printifeqn( "exists dd:" +
	 * sortdata.getName() + ". dd in " + m1 + "&&" + printf(is_pn, printf(head, m1))
	 * + "&&" + printf(is_ssr, printf(frt, printf(pv, printf(head, m1)))),
	 * printtruef(printf("ssrecviolation", m1)), ""); s = s + printifeqn(
	 * "!(exists dd:" + sortdata.getName() + ". dd in " + m1 + "&&" + printf(is_pn,
	 * printf(head, m1)) + "&&" + printf(is_ssr, printf(frt, printf(pv, printf(head,
	 * m1)))) + ")", printfalsef(printf("ssrecviolation", m1)), ""); } else if
	 * (AbstractTranslationAlg.id_op == IDOperaion.RECONSTRUCTION.getVal()) { s = s
	 * + printifeqn( printf(is_pn, printf(head, m1)) + "&&" + "(" + printf(is_sss,
	 * printf(frt, printf(pv, printf(head, m1)))) + "||" + printf(is_ssc,
	 * printf(frt, printf(pv, printf(head, m1)))) + ")", printf("list2bag", m1, b),
	 * printf("list2bag", printf(tail, m1), b + "+{" + printf(snd, printf(pv,
	 * printf(head, m1))) + ":1}")); s = s + printifeqn( printf("!(" + is_pn,
	 * printf(head, m1)) + ") || !(" + printf(is_sss, printf(frt, printf(pv,
	 * printf(head, m1)))) + ") || !(" + printf(is_ssc, printf(frt, printf(pv,
	 * printf(head, m1)))) + ")", printf("list2bag", m1, b), printf("list2bag",
	 * printf(tail, m1), b)); s = s + printifeqn(m1 + "== []", printf("list2bag",
	 * m1, b), b); s = s + printifeqn( "(exists " + i + ":Nat. " + i + " in " + b +
	 * " && count(" + i + "," + b + ")>= " + TH + ")", printf("is_recostructed", b),
	 * "true"); s = s + printifeqn( "!(exists " + i + ":Nat. " + i + " in " + b +
	 * " && count(" + i + "," + b + ")>= " + TH + ")", printf("is_recostructed", b),
	 * "false"); } } else if (AbstractTranslationAlg.id_op ==
	 * IDOperaion.ENCRYPTION.getVal()) { s = s + printifeqn( printf(is_pn,
	 * printf(head, m1)) + " && " + printf(is_dk, printf(frt, printf(pv,
	 * printf(head, m1)))) + " && " + printf(snd, printf(pv, printf(head, m1))) +
	 * " == " + id, printtruef(printf("haskey", m1, id)), ""); s = s + printifeqn(
	 * "!(" + printf(is_pn, printf(head, m1)) + " )|| !(" + printf(is_dk,
	 * printf(frt, (printf(pv, printf(head, m1))))) + ") || !(" + printf(snd,
	 * printf(pv, printf(head, m1))) + " == " + id + ")", printf("haskey", m1, id),
	 * printf("haskey", printf(tail, m1), id)); s = s + printifeqn(m1 + "== []",
	 * printfalsef(printf("haskey", m1, id)), ""); s = s + printifeqn( printf(is_pn,
	 * printf(head, m1)) + " && " + printf(is_cip, printf(frt, printf(pv,
	 * printf(head, m1)))) + " && " + printf(snd, printf(pv, printf(head, m1))) +
	 * " == " + id, printtruef(printf("hascipher", m1, id)), ""); s = s +
	 * printifeqn( "!(" + printf(is_pn, printf(head, m1)) + " )|| !(" +
	 * printf(is_cip, printf(frt, printf(pv, printf(head, m1)))) + ") || !(" +
	 * printf(snd, printf(pv, printf(head, m1))) + " == " + id + ")",
	 * printf("hascipher", m1, id), printf("hascipher", printf(tail, m1), id)); s =
	 * s + printifeqn(m1 + "== []", printfalsef(printf("hascipher", m1, id)), ""); s
	 * = s + printifeqn(printf("haskey", m1, id) + " && " + printf("hascipher", m1,
	 * id), printtruef(printf("encryptionviolation", m1, id)), ""); s = s +
	 * printifeqn("!" + printf("haskey", m1, id) + " || !" + printf("hascipher", m1,
	 * id), printfalsef(printf("encryptionviolation", m1, id)), "");
	 * 
	 * } return s; }
	 */

	/*
	 * public static List<String> childTaskProcess(Process partecipant, mCRL2 mcrl2,
	 * List<String> childs) { if (!partecipant.hasChild()) return childs; for (int i
	 * = 0; i < partecipant.getLength(); i++) { AbstractProcess child =
	 * mcrl2.identifyAbstractProcess(partecipant.getChildName(i)); if (child ==
	 * null) continue; if (child.getClass().equals(TaskProcess.class))
	 * childs.add(((TaskProcess) child).getAction().getName()); else
	 * childTaskProcess(((Process) child), mcrl2, childs); } return childs; }
	 */

	/*
	 * public ParticipantProcess identifyMyParticipant(TaskProcess t) { for
	 * (ParticipantProcess p : getParcipantProcesses()) { List<String> childs = new
	 * ArrayList<String>(); childs.addAll(mCRL2.childTaskProcess(p.getProcess(),
	 * this, childs)); if (childs.contains(t.getAction().getName())) { return p; } }
	 * return null; }
	 */

	/*
	 * public Action identifyRecostructionTask() { for (Action a : actions) { if
	 * (a.getPet()!= null) { if (a.getPet().equals(PETLabel.SSRECONTRUCTION.name()))
	 * return a; } } return null; }
	 */

	/*
	 * public Set<Integer> identifySSSharingTask() { Set<Pair<String, AbstractTaskPET>>
	 * pair = sortdata.getPrivatePair(); Set<Integer> set = new HashSet<Integer>();
	 * for (Pair<String, AbstractTaskPET> p : pair) { if
	 * (p.getRight().getPET().equals(PETLabel.SSSHARING))
	 * set.add(p.getRight().getIdPet()); } return set; }
	 */

	/*
	 * public Set<Integer> identifySSComputationTask() { Set<Pair<String,
	 * AbstractTaskPET>> pair = sortdata.getPrivatePair(); Set<Integer> set = new
	 * HashSet<Integer>(); for (Pair<String, AbstractTaskPET> p : pair) { if
	 * (p.getRight().getPET().equals(PETLabel.SSCOMPUTATION))
	 * set.add(p.getRight().getIdPet()); } return set; }
	 */

}