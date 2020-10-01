package spec.mcrl2obj;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import algo.AbstractTranslationAlg;
import algo.IDOperaion;
import io.pet.PET;
import io.pet.PETLabel;
import io.pet.SSsharing;
import spec.ISpec;

/*
 * Class that define a mcrl2 spec object 
 */
public class mCRL2 implements ISpec {

	public static final String recostruct = "RECOSTRUCTED";
	public static final String norecostruct = "NOTRECOSTRUCTED";
	public static final String violation = "VIOLATION";
	public static final String sss = "sssharing";
	public static final String is_sss = "is_sssharing";
	public static final String ssc = "sscomputation";
	public static final String is_ssc = "is_sscomputation";
	public static final String ssr = "ssrecostruction";
	public static final String is_ssr = "is_ssrecostruction";
	public static final String node = "node";
	public static final String is_n = "is_node";
	public static final String pnode = "pnode";
	public static final String is_pn = "is_pnode";
	public static final String v = "value";
	public static final String pv = "pvalue";
	public static final String dk = "decondingkey";
	public static final String is_dk = "is_decondingkey";
	public static final String cip = "cipher";
	public static final String is_cip = "is_cipher";
	public static final String unionf = "union";
	public static final String emptyf = "empty";
	public static final String head = "head";
	public static final String tail = "tail";
	public static final String frt = "frt";
	public static final String snd = "snd";
	public static final String pair = "pair";
	public static final String TH = "TH";
	private Sort sortnat = new Sort("Nat");
	protected static StructSort sortname = new StructSort("Name");
	protected static StructSort sortpname = new StructSort("PName");
	protected static StructSort sortprivacy = new StructSort("Privacy");
	protected static StructSort sortdata = new StructSort("Data");
	protected static Sort sortmemory = new Sort("Memory");
	protected static Sort sortbool = new Sort("Bool");
	protected static Sort sortbagnat = new Sort("Bag(Nat)");
	public static final String eps = "eps";
	private Set<Action> actions;
	private Set<Action> allow;
	private Set<CommunicationFunction> comm;
	private Set<Action> hide;
	private Set<AbstractProcess> processes;
	private Set<String> initSet;
	private static int id = 0;

	public mCRL2() {
		this.actions = new HashSet<Action>();
		this.allow = new HashSet<Action>();
		if (AbstractTranslationAlg.id_op == IDOperaion.SSSHARING.getVal()
				|| AbstractTranslationAlg.id_op == IDOperaion.ENCRYPTION.getVal())
			this.allow.add(Action.setVIOLATOINAction());
		else if (AbstractTranslationAlg.id_op == IDOperaion.RECONSTRUCTION.getVal()) {
			this.actions.add(Action.setRECOSTRUCTIONAction());
			this.actions.add(new Action(norecostruct));
			this.allow.add(Action.setRECOSTRUCTIONAction());
			this.allow.add(new Action(norecostruct));
		}
		
		sortname.addType(emptyf);
		this.comm = new HashSet<CommunicationFunction>();
		// this.sorts = new HashSet<Sort>();
		this.hide = new HashSet<Action>();
		this.processes = new HashSet<AbstractProcess>();
		this.initSet = new HashSet<String>();
	}

	public void addCommunicaitonFunction(CommunicationFunction f) {
		this.comm.add(f);
	}

	/*
	 * public Set<Action> getActions() { return actions; }
	 */
	//
	public Action getActionByName(String name) {
		for (Action a : actions) {
			if (a.getName().equals(name))
				return a;
		}
		return null;
	}

	public void addAction(Action... action) {
		for (Action a : action) {
			boolean t = false;
			for (Action act : this.actions) {
				if (act.getName().equals(a.getName()) && act.getSortString().equals(a.getSortString()))
					t = true;
			}
			if (!t)
				this.actions.add(a);
		}
	}

	public String getValueTreshold() {
		for (Pair<String, PET> p : sortdata.getPrivatePair()) {
			if (p.getRight().getPET().equals(PETLabel.SSSHARING))
				return String.valueOf(((SSsharing) p.getRight()).getThreshold());
		}
		return "";
	}

	// Data = struct data1 |... | datan;
	public StructSort getSortName() {
		return sortname;
	}

	public void setSortPName() {
		if (AbstractTranslationAlg.id_op == IDOperaion.SSSHARING.getVal()
				|| AbstractTranslationAlg.id_op == IDOperaion.RECONSTRUCTION.getVal()) {
			sortpname.addType(printf(sss, sortname.getName()) + "?" + is_sss);
			sortpname.addType(printf(ssc, sortname.getName()) + "?" + is_ssc);
			sortpname.addType(printf(ssr, sortname.getName()) + "?" + is_ssr);
		} else if (AbstractTranslationAlg.id_op == IDOperaion.ENCRYPTION.getVal()) {
			sortpname.addType(printf(dk, sortname.getName()) + "?" + is_dk);
			sortpname.addType(printf(cip, sortname.getName()) + "?" + is_cip);
		}
	}

	public StructSort getSortPName() {
		return sortpname;
	}

	public void setSortPrivacy() {
		sortprivacy.addType("pair(frt:" + sortpname.getName() + ",snd:Nat)");
	}

	public StructSort getSortPrivacy() {
		return sortprivacy;
	}

	public void setSortData() {
		sortdata.addType(printf(node, v + ":" + sortname.getName()) + "?" + is_n);
		sortdata.addType(eps);
	}

	public StructSort getSortData() {
		return sortdata;
	}

	public void setSortMemory() {
		if (AbstractTranslationAlg.id_op == IDOperaion.TASK.getVal()
				|| AbstractTranslationAlg.id_op == IDOperaion.PARTICIPANT.getVal())
			sortmemory.addType(" Set(" + sortdata.getName() + ")");
		else if (AbstractTranslationAlg.id_op == IDOperaion.SSSHARING.getVal()
				|| AbstractTranslationAlg.id_op == IDOperaion.RECONSTRUCTION.getVal()
				|| AbstractTranslationAlg.id_op == IDOperaion.ENCRYPTION.getVal()) {
			sortmemory.addType(" List(" + sortdata.getName() + ")");
		}
	}

	public Sort getSortMemory() {
		return sortmemory;
	}

	// Predefined mcrl2 sort
	public Sort getSortBool() {
		return sortbool;
	}

	public void addAllow(Action... allow) {
		for (Action a : allow) {
			boolean t = false;
			for (Action act : this.allow) {
				if (act.getName().equals(a.getName()))
					t = true;
			}
			if (!t)
				this.allow.add(a);
		}
	}

	public void addHide(Action... hide) {
		for (Action a : hide) {
			boolean t = false;
			for (Action act : this.hide) {
				if (act.getName().equals(a.getName()))
					t = true;
			}
			if (!t)
				this.hide.add(a);
		}
	}

	public Set<AbstractProcess> getProcesses() {
		return processes;
	}

	public Process getPartcipant(String name) {
		for (AbstractProcess ap : processes) {
			if (ap.getClass().equals(PartecipantProcess.class) && ((PartecipantProcess) ap).getId().equals(name))
				return (Process) ap;
		}
		return null;
	}

	public Set<PartecipantProcess> getParcipantProcesses() {
		Set<PartecipantProcess> p = new HashSet<PartecipantProcess>();
		for (AbstractProcess ap : processes) {
			if (ap.getClass().equals(PartecipantProcess.class))
				p.add((PartecipantProcess) ap);
		}
		return p;
	}

	public void addProcess(AbstractProcess process) {
		this.processes.add(process);
	}

	public void addProcesses(Set<AbstractProcess> processes) {
		this.processes.addAll(processes);
	}

	public void addInitSet(String... initSet) {
		for (String s : initSet)
			this.initSet.add(s);
	}

	public void removePinInitSet(String name) {
		this.initSet.remove(name);
	}

	private String printSorts() {
		String s = "";
		if (!sortname.isEmpty())
			s = s + sortname.toString() + ";\n";
		if (!sortpname.isEmpty())
			s = s + sortpname.toString() + ";\n";
		if (!sortprivacy.isEmpty())
			s = s + sortprivacy.toString() + ";\n";
		if (!sortdata.isEmpty())
			s = s + sortdata.toString() + ";\n";
		if (!sortmemory.isEmpty())
			s = s + sortmemory.toString() + ";\n";
		return s;
	}

	@Override
	public String toString() {
		String s = "sort ";
		s = s + printSorts();
		s = s + memoryToString();
		s = s + "act" + "\n";
		if (AbstractTranslationAlg.id_op == IDOperaion.SSSHARING.getVal()
				|| AbstractTranslationAlg.id_op == IDOperaion.ENCRYPTION.getVal())
			s = s + "VIOLATION;\n";
		Map<String, Set<String>> classact = classifyaction();
		for (Entry<String, Set<String>> entry : classact.entrySet()) {
			int i = 0;
			for (String a : entry.getValue()) {
				s = s + a;
				if (i != entry.getValue().size() - 1)
					s = s + ",";
				i++;
			}
			if (entry.getKey().equals(""))
				s = s + ";\n";
			else
				s = s + ": " + entry.getKey() + ";\n";
		}

		s = s + "proc" + "\n";
		for (AbstractProcess process : processes) {
			s = s + process.toString() + ";\n";
		}

		s = s + "init ";
		int par = 0;
		if (!hide.isEmpty()) {
			s = s + "hide ({";
			int i = 0;
			for (Action a : hide) {
				s = s + a.getName();
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
				s = s + a.getName();
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
		int i = 0;
		for (String name : initSet) {
			s = s + name;
			if (i != initSet.size() - 1)
				s = s + "||";
			i++;
		}
		while (par != 0) {
			s = s + ")";
			par--;
		}

		return s + ";";
	}

	private Map<String, Set<String>> classifyaction() {
		// n of parameters - action
		Map<String, Set<String>> classact = new HashMap<String, Set<String>>();
		for (Action a : actions) {
			if (classact.containsKey(a.getSortString()))
				classact.get(a.getSortString()).add(a.getName());
			else {
				Set<String> actions = new HashSet<String>();
				actions.add(a.getName());
				classact.put(a.getSortString(), actions);
			}
		}
		return classact;
	}

	public void taureduction() {
		List<String> processtoremove = new ArrayList<String>();
		for (AbstractProcess p : processes) {
			if ((p.getClass().equals(Process.class) && ((Process) p).hasChild())
					|| p.getClass().equals(PartecipantProcess.class)) {

				Process process;
				if (p.getClass().equals(PartecipantProcess.class))
					process = ((PartecipantProcess) p).getProcess();
				else
					process = (Process) p;

				List<String> newchilds = new ArrayList<String>();
				for (int i = 0; i < process.getLength(); i++) {
					AbstractProcess child = identifyAbstractProcess(process.getChildName(i));
					if (child == null) {
						newchilds.add(process.getChildName(i));
						continue;
					}
					if (child.getClass().equals(Process.class)) {
						if (((Process) child).isActivity() && ((Process) child).getAction().isTau()) {
							processtoremove.add(process.getChildName(i));
						} else
							newchilds.add(process.getChildName(i));
					} else
						newchilds.add(process.getChildName(i));
				}
				process.setChild(newchilds);
			}

		}
		Set<AbstractProcess> newprocess = new HashSet<AbstractProcess>();
		for (AbstractProcess p : processes) {
			if (!processtoremove.contains(p.getName())) {
				newprocess.add(p);

			}
		}
		this.processes = newprocess;
		emptyprocess();
	}

	private void emptyprocess() {
		boolean change = true;
		while (change) {
			change = false;
			for (AbstractProcess p : processes) {

				if (p.getClass().equals(Process.class) || p.getClass().equals(PartecipantProcess.class)) {
					Process process;
					if (p.getClass().equals(Process.class))
						process = (Process) p;
					else
						process = ((PartecipantProcess) p).getProcess();
					// Empty process
					if (process.getAction() == null && !process.hasChild()) {
						this.processes.remove(p);
						change = true;
						break;
					} else if (process.hasChild()) {
						// Process thatcontainedemptyprocess or single references to process are removed
						List<String> newchild = new ArrayList<String>();
						boolean removec = false;
						for (int i = 0; i < process.getLength(); i++) {
							if (identifyAbstractProcess(process.getChildName(i)) != null
									|| process.getInsideDef(process.getChildName(i)) != null)
								newchild.add(process.getChildName(i));
							else
								removec = true;
						}
						if (removec) {
							process.setChild(newchild);
							change = true;
							break;
						}

					}
				}

			}
		}
	}

	public TaskProcess identifyTaskProcessFromAction(Action a) {
		for (AbstractProcess p : processes) {
			if (p.getClass().equals(TaskProcess.class) && ((TaskProcess) p).getAction().equals(a))
				return (TaskProcess) p;
		}
		return null;
	}

	public AbstractProcess identifyAbstractProcess(String name) {
		for (AbstractProcess p : processes) {
			if (p.getName().equals(name))
				return p;
		}
		return null;
	}

	/*
	 * return true: if exist a process that represent a task or a partecipant with
	 * this name
	 */
	public boolean containt(String s) {
		for (AbstractProcess ab : processes) {
			if ((ab.getClass().equals(TaskProcess.class) && ((TaskProcess) ab).getAction() != null
					&& ((TaskProcess) ab).getAction().getId().equals(s))
					|| (ab.getClass().equals(PartecipantProcess.class) && ((PartecipantProcess) ab).getId().equals(s)))
				return true;
		}
		return false;
	}

	public void removeProcess(String name) {
		for (AbstractProcess ap : processes) {
			if (ap.getName().equals(name)) {
				processes.remove(ap);
				break;
			}
		}

	}

	public String toStringPartecipants() {
		String s = "PARTECIPANTS:\n";
		for (AbstractProcess ap : processes) {
			if (ap.getClass().equals(PartecipantProcess.class))
				s = s + " ID: " + ((PartecipantProcess) ap).getId() + " NAME: "
						+ ((PartecipantProcess) ap).getPartecipantName() + "\n";
		}
		return s;
	}

	public String toStringTasks() {
		String s = "All task in the model:\n";
		for (Action a : actions) {
			if (a.getId() != null && !a.getId().equals("")) {
				s = s + "ID: " + a.getId() + " ";
				if (!a.getSecondName().isEmpty())
					s = s + " NAME: " + a.getSecondName();
				s = s + "\n";
			}
		}
		return s;
	}

	public String toStringData() {
		String s = "All data in the model ";
		for (String t : sortname.getTypes())
			s = s + t.toString() + "\n";

		return s;
	}

	public String toFile(String fileName) {
		File file = new File(fileName + ".mcrl2");
		while (file.exists())
			file = new File(fileName + (id++) + ".mcrl2");

		try (BufferedWriter output = new BufferedWriter(new FileWriter(file))) {
			output.write(toString());
			output.close();
		} catch (Exception e) {
			System.err.println("Faile to generate " + file);
		}
		return file.getName().replace(".mcrl2", "");
	}

	// print namef = domain1 # domain2 # ... # domainn -> codomain
	private String printMap(String namef, Sort codomain, Sort... domain) {
		String s = namef + ":";
		for (int i = 0; i < domain.length; i++) {
			s = s + domain[i].getName();
			if (i != domain.length - 1)
				s = s + "#";
		}
		s = s + "->" + codomain.getName() + "; \n";
		return s;
	}

	// Print (ifs) -> thens = eqs
	public static String printifeqn(String ifs, String thens, String eqs) {
		String s = "";
		if (!eqs.isEmpty())
			s = "(" + ifs + ") -> " + thens + " = " + eqs + ";\n";
		else
			s = "(" + ifs + ") -> " + thens;
		return s;
	}

	// Print f = true;
	private String printtruef(String f) {
		return f + " = " + true + ";\n";
	}

	// Print f = false;
	public static String printfalsef(String f) {
		return f + "=" + false + ";\n";
	}

	// Print f(arg1,arg2,...,argn)
	public static String printf(String f, String... arg) {
		String s = f + "(";
		for (int i = 0; i < arg.length; i++) {
			s = s + arg[i];
			if (i != arg.length - 1)
				s = s + ",";
		}
		s = s + ")";
		return s;
	}

	private String printvar(Sort domain, String... vars) {
		String s = "";
		for (int i = 0; i < vars.length; i++) {
			s = s + vars[i];
			if (i != vars.length - 1)
				s = s + ",";
		}
		s = s + " : " + domain.getName() + ";\n";
		return s;
	}

	// Print in output of all the function in map, var and eqn, remember to modify 1
	// with the real thresold value
	private String memoryToString() {
		String m1 = Action.setTemporaryAction().getName();
		String m2 = Action.setTemporaryAction().getName();
		String pn = Action.setTemporaryAction().getName();
		String n = Action.setTemporaryAction().getName();
		String p = Action.setTemporaryAction().getName();
		String data = Action.setTemporaryAction().getName();
		String i = Action.setTemporaryAction().getName();
		String id = Action.setTemporaryAction().getName();
		String b = Action.setTemporaryAction().getName();
		String s = "map \r\n";
		s = s + printMap("union", sortmemory, sortmemory, sortmemory);
		/*if(AbstractTranslationAlg.id_op == IDOperaion.TASK.getVal() || AbstractTranslationAlg.id_op == IDOperaion.PARTICIPANT.getVal()) {
			s = s + printMap(emptyf, sortbool,sortmemory);
		}else */
			s = s + printMap("empty", sortbool, sortdata);
		if (AbstractTranslationAlg.id_op == IDOperaion.SSSHARING.getVal()) {
			s = s + TH + " : " + sortnat.getName() + ";\n";
			s = s + printMap("sssharingviolation", sortbool, sortnat, sortmemory);
			s = s + printMap("sslist", sortnat, sortnat, sortmemory, sortnat);
			s = s + printMap("sscompviolation", sortbool, sortnat, sortmemory);
			s = s + printMap("sscomp", sortnat, sortnat, sortmemory, sortnat);
			s = s + printMap("ssrecviolation", sortbool, sortmemory);
		} else if (AbstractTranslationAlg.id_op == IDOperaion.ENCRYPTION.getVal()) {
			s = s + printMap("haskey", sortbool, sortmemory, sortnat);
			s = s + printMap("hascipher", sortbool, sortmemory, sortnat);
			s = s + printMap("encryptionviolation", sortbool, sortmemory, sortnat);
		} else if (AbstractTranslationAlg.id_op == IDOperaion.RECONSTRUCTION.getVal()) {
			s = s + TH + " : " + sortnat.getName() + ";\n";
			//s = s + printMap("rlist", sortnat, sortmemory, sortnat);
			//s = s + printMap("is_recostructed", sortbool, sortmemory);
			s = s + printMap("list2bag", sortbagnat, sortmemory,sortbagnat);
			s = s + printMap("is_recostructed", sortbool, sortbagnat);
		}
		s = s + "var\r\n";
		s = s + printvar(sortmemory, m1, m2);
		s = s + printvar(sortdata, data);
		if (AbstractTranslationAlg.id_op == IDOperaion.SSSHARING.getVal()
				|| AbstractTranslationAlg.id_op == IDOperaion.RECONSTRUCTION.getVal()
				|| AbstractTranslationAlg.id_op == IDOperaion.ENCRYPTION.getVal()) {
			s = s + printvar(sortpname, pn);
			s = s + printvar(sortname, n);
			s = s + printvar(sortprivacy, p);
			s = s + printvar(sortnat, i, id);
			s = s + printvar(sortbagnat, b);
		}
		s = s + "eqn \n";
		if (AbstractTranslationAlg.id_op == IDOperaion.TASK.getVal()
				|| AbstractTranslationAlg.id_op == IDOperaion.PARTICIPANT.getVal()) {
			s = s + printf(unionf, m1, m2) + "=" + m1 + " + " + m2 + ";\n";
			s = s + printifeqn(data + "== " + eps, printtruef(printf(emptyf, data)), "");
			s = s + printifeqn(data + "!= " + eps, printfalsef(printf(emptyf, data)), "");
			//s = s + printf(emptyf, m1) + "= { dd :" + sortdata.getName() + " | ({ dd } * " + m1 + "!= {}) && (dd != "
				//	+ eps + ")} == {};\n";
		} else if (AbstractTranslationAlg.id_op == IDOperaion.SSSHARING.getVal()
				|| AbstractTranslationAlg.id_op == IDOperaion.RECONSTRUCTION.getVal()
				|| AbstractTranslationAlg.id_op == IDOperaion.ENCRYPTION.getVal()) {
			s = s + printifeqn(printf(head, m2) + " in " + m1, printf(unionf, m1, m2),
					printf(unionf, m1, printf(tail, m2)));
			s = s + printifeqn("!(" + printf(head, m2) + " in " + m1 + ")", printf(unionf, m1, m2),
					printf(unionf, m1 + " <| " + printf(head, m2), printf(tail, m2)));
			s = s + printifeqn(m2 + "== [] ", printf(unionf, m1, m2), m1);
			s = s + printifeqn(m1 + "== [] ", printf(unionf, m1, m2), m2);
			s = s + printifeqn(data + "== " + eps, printtruef(printf(emptyf, data)), "");
			s = s + printifeqn(data + "!= " + eps, printfalsef(printf(emptyf, data)), "");
					}
		if (AbstractTranslationAlg.id_op == IDOperaion.SSSHARING.getVal()
				|| AbstractTranslationAlg.id_op == IDOperaion.RECONSTRUCTION.getVal()) {
			s = s + TH + "=" + getValueTreshold() + ";\n";
			// if its just sssharing checking
			if (AbstractTranslationAlg.id_op == IDOperaion.SSSHARING.getVal()) {
				s = s + printifeqn(printf("sslist", id, m1, String.valueOf(0)) + ">=" + TH,
						printtruef(printf("sssharingviolation", id, m1)), "");
				s = s + printifeqn(printf("sslist", id, m1, String.valueOf(0)) + "<" + TH,
						printfalsef(printf("sssharingviolation", id, m1)), "");
				s = s + printifeqn(
						printf(is_pn, printf(head, m1)) + " && "
								+ printf(is_sss, printf(frt, printf(pv, printf(head, m1)))) + " && "
								+ printf(snd, printf(pv, printf(head, m1))) + " == " + id,
						printf("sslist", id, m1, i),
						printf("sslist", id, printf(tail, m1), String.valueOf(i) + "+" + 1));
				s = s + printifeqn(
						printf("!(" + is_pn, printf(head, m1)) + ") || !("
								+ printf(is_sss, printf(frt, printf(pv, printf(head, m1)))) + ") || "
								+ printf(snd, printf(pv, printf(head, m1))) + " != " + id,
						printf("sslist", id, m1, i), printf("sslist", id, printf(tail, m1), i));
				s = s + printifeqn(m1 + "== []", printf("sslist", id, m1, i), i);
				s = s + printifeqn(printf("sscomp", id, m1, String.valueOf(0)) + ">=" + TH,
						printtruef(printf("sscompviolation", id, m1)), "");
				s = s + printifeqn(printf("sscomp", id, m1, String.valueOf(0)) + "<" + TH,
						printfalsef(printf("sscompviolation", id, m1)), "");
				s = s + printifeqn(
						printf(is_pn, printf(head, m1)) + " && "
								+ printf(is_ssc, printf(frt, printf(pv, printf(head, m1)))) + " && "
								+ printf(snd, printf(pv, printf(head, m1))) + " == " + id,
						printf("sscomp", id, m1, i),
						printf("sscomp", id, printf(tail, m1), String.valueOf(i) + "+" + 1));
				s = s + printifeqn(
						printf("!(" + is_pn, printf(head, m1)) + ") || !("
								+ printf(is_ssc, printf(frt, printf(pv, printf(head, m1)))) + ") || "
								+ printf(snd, printf(pv, printf(head, m1))) + " != " + id,
						printf("sscomp", id, m1, i), printf("sscomp", id, printf(tail, m1), i));
				s = s + printifeqn(m1 + "== []", printf("sscomp", id, m1, i), i);
				s = s + printifeqn(
						"exists dd:" + sortdata.getName() + ". dd in " + m1 + "&&" + printf(is_pn, printf(head, m1))
								+ "&&" + printf(is_ssr, printf(frt, printf(pv, printf(head, m1)))),
						printtruef(printf("ssrecviolation", m1)), "");
				s = s + printifeqn(
						"!(exists dd:" + sortdata.getName() + ". dd in " + m1 + "&&" + printf(is_pn, printf(head, m1))
								+ "&&" + printf(is_ssr, printf(frt, printf(pv, printf(head, m1)))) + ")",
						printfalsef(printf("ssrecviolation", m1)), "");
			} else if (AbstractTranslationAlg.id_op == IDOperaion.RECONSTRUCTION.getVal()) {
					s = s + printifeqn(printf(is_pn,printf(head, m1)) + "&&" + "("+printf(is_sss, printf(frt, printf(pv, printf(head, m1)))) + 
						"||"+ printf(is_ssc, printf(frt, printf(pv, printf(head, m1)))) +")", printf("list2bag", m1,b), 
						printf("list2bag", printf(tail, m1),b + "+{"+printf(snd, printf(pv, printf(head, m1)))+":1}"));
				s = s + printifeqn(
						printf("!(" + is_pn, printf(head, m1)) + ") || !("
								+ printf(is_sss, printf(frt, printf(pv, printf(head, m1)))) + ") || !("
								+ printf(is_ssc, printf(frt, printf(pv, printf(head, m1)))) + ")"
								,
						printf("list2bag", m1, b), printf("list2bag", printf(tail, m1), b));
				s = s + printifeqn(m1 + "== []", printf("list2bag", m1, b), b);
				s = s + printifeqn("(exists " +i +":Nat. " + i + " in " + b + " && count(" + i +","+b +")>= "+ TH+")",printf("is_recostructed", b) , "true");
				s = s + printifeqn("!(exists " +i +":Nat. " + i + " in " + b + " && count(" + i +","+b +")>= "+ TH+")",printf("is_recostructed", b) , "false");
			}
		} else if (AbstractTranslationAlg.id_op == IDOperaion.ENCRYPTION.getVal()) {
			s = s + printifeqn(
					printf(is_pn, printf(head, m1)) + " && " + printf(is_dk, printf(frt, printf(pv, printf(head, m1))))
							+ " && " + printf(snd, printf(pv, printf(head, m1))) + " == " + id,
					printtruef(printf("haskey", m1, id)), "");
			s = s + printifeqn(
					"!(" + printf(is_pn, printf(head, m1)) + " )|| !("
							+ printf(is_dk, printf(frt, (printf(pv, printf(head, m1))))) + ") || !("
							+ printf(snd, printf(pv, printf(head, m1))) + " == " + id + ")",
					printf("haskey", m1, id), printf("haskey", printf(tail, m1), id));
			s = s + printifeqn(m1 + "== []", printfalsef(printf("haskey", m1, id)), "");
			s = s + printifeqn(
					printf(is_pn, printf(head, m1)) + " && " + printf(is_cip, printf(frt, printf(pv, printf(head, m1))))
							+ " && " + printf(snd, printf(pv, printf(head, m1))) + " == " + id,
					printtruef(printf("hascipher", m1, id)), "");
			s = s + printifeqn(
					"!(" + printf(is_pn, printf(head, m1)) + " )|| !("
							+ printf(is_cip, printf(frt, printf(pv, printf(head, m1)))) + ") || !("
							+ printf(snd, printf(pv, printf(head, m1))) + " == " + id + ")",
					printf("hascipher", m1, id), printf("hascipher", printf(tail, m1), id));
			s = s + printifeqn(m1 + "== []", printfalsef(printf("hascipher", m1, id)), "");
			s = s + printifeqn(printf("haskey", m1, id) + " && " + printf("hascipher", m1, id),
					printtruef(printf("encryptionviolation", m1, id)), "");
			s = s + printifeqn("!" + printf("haskey", m1, id) + " || !" + printf("hascipher", m1, id),
					printfalsef(printf("encryptionviolation", m1, id)), "");

		}
		return s;
	}

	public static List<String> childTaskProcess(Process partecipant, mCRL2 mcrl2, List<String> childs) {
		if (!partecipant.hasChild())
			return childs;
		for (int i = 0; i < partecipant.getLength(); i++) {
			AbstractProcess child = mcrl2.identifyAbstractProcess(partecipant.getChildName(i));
			if (child == null)
				continue;
			if (child.getClass().equals(TaskProcess.class))
				childs.add(((TaskProcess) child).getAction().getName());
			else
				childTaskProcess(((Process) child), mcrl2, childs);
		}
		return childs;
	}

	public PartecipantProcess identifyMyParticipant(TaskProcess t) {
		for (PartecipantProcess p : getParcipantProcesses()) {
			List<String> childs = new ArrayList<String>();
			childs.addAll(mCRL2.childTaskProcess(p.getProcess(), this, childs));
			if (childs.contains(t.getAction().getName())) {
				return p;
			}
		}
		return null;
	}

	public Action identifyRecostructionTask() {
		for (Action a : actions) {
			if (!a.getPet().isEmpty()) {
				if (a.getPet().equals(PETLabel.SSRECONTRUCTION.name()))
					return a;
			}
		}
		return null;
	}

	public Set<Integer> identifySSSharingTask() {
		Set<Pair<String, PET>> pair = sortdata.getPrivatePair();
		Set<Integer> set = new HashSet<Integer>();
		for (Pair<String, PET> p : pair) {
			if (p.getRight().getPET().equals(PETLabel.SSSHARING))
				set.add(p.getRight().getIdPet());
		}
		return set;
	}

	public Set<Integer> identifySSComputationTask() {
		Set<Pair<String, PET>> pair = sortdata.getPrivatePair();
		Set<Integer> set = new HashSet<Integer>();
		for (Pair<String, PET> p : pair) {
			if (p.getRight().getPET().equals(PETLabel.SSCOMPUTATION))
				set.add(p.getRight().getIdPet());
		}
		return set;
	}

}