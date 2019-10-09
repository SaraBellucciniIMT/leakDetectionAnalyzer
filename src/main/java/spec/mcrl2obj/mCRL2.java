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

import io.pet.PET;
import spec.ISpec;

/*
 * Class that define a mcrl2 spec object 
 */
public class mCRL2 implements ISpec {

	public Set<Sort> sorts;
	private Set<Action> actions;
	private Set<Action> allow;
	private Set<CommunicationFunction> comm;
	private Set<Action> hide;
	private Set<AbstractProcess> processes;
	private Set<String> initSet;
	private static int id = 0;
	private Map<PET, Set<String>> sensibledata;

	public mCRL2() {
		this.actions = new HashSet<Action>();
		this.allow = new HashSet<Action>();
		this.comm = new HashSet<CommunicationFunction>();
		this.sorts = new HashSet<Sort>();
		this.hide = new HashSet<Action>();
		this.processes = new HashSet<AbstractProcess>();
		this.initSet = new HashSet<String>();

	}

	public void addCommunicaitonFunction(CommunicationFunction f) {
		this.comm.add(f);
	}

	public Set<Action> getActions() {
		return actions;
	}

	public Map<PET, Set<String>> getSensibleData() {
		return this.sensibledata;
	}

	public void setSensibleData(Map<PET, Set<String>> s) {
		this.sensibledata = s;
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

	public void addSort(Sort... sorts) {
		for (int i = 0; i < sorts.length; i++)
			this.sorts.add(sorts[i]);
	}

	public Set<Action> getAllow() {
		return allow;
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

	public Set<CommunicationFunction> getComm() {
		return comm;
	}

	public void setComm(CommunicationFunction comm) {
		this.comm.add(comm);
	}

	public Set<Action> getHide() {
		return hide;
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

	public Set<String> getInitSet() {
		return initSet;
	}

	public void addInitSet(String... initSet) {
		for (String s : initSet)
			this.initSet.add(s);
	}

	public void removePinInitSet(String name) {
		this.initSet.remove(name);
	}

	@Override
	public String toString() {
		String s = "";
		for (Sort sort : sorts) {
			if (!sort.getName().equalsIgnoreCase("bool"))
				s = s + sort.toString() + ";\n";
		}
		s = s + memoryToString();
		s = s + "act" + "\n";
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
									|| process.inInsideDef(process.getChildName(i)) != null)
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
			if ((ab.getClass().equals(TaskProcess.class) && ((TaskProcess) ab).getAction().getId().equals(s))
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
			if (a.getId() != null && !a.getId().equals(""))
				s = s + "ID: " + a.getId() + " " + " NAME: " + a.getSecondName() + "\n";
		}
		return s;
	}

	public String toStringData() {
		String s = "All data in the model ";
		for (Sort sort : sorts) {
			if (sort.getName().equals("Data")) {
				for (String t : sort.getTypes())
					s = s + t.toString() + "\n";
				break;
			}
		}
		return s;
	}

	public String toFile(String fileName) {
		File file = new File(fileName + ".mcrl2");
		while (file.exists())
			file = new File(fileName + (id++) + ".mcrl2");

		try (BufferedWriter output = new BufferedWriter(new FileWriter(file))) {
			output.write(toString());
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return file.getName().replace(".mcrl2", "");
	}

	public String memoryToString() {
		//action names
		int i=0;
		String a1 = "m"+i;
		Sort data = null;
		for (Sort sort : sorts) {
			if (sort.getName().equalsIgnoreCase("Data")) {
				data = sort;
				continue;
			}
		}
		while(data.getTypes().contains(a1)) {
			i++;
			a1 = "m" + i;
		}
		String a2 = "m" + (i+1);
		while(data.getTypes().contains(a2)) {
			i++;
			a2 = "m" + (i+1);
			
		}
			
		String s = "map \r\n" + "union : Memory # Memory -> Memory;\r\n" + "var\r\n"
				+ a1+","+a2+": Memory;\r\n" + "eqn \r\n" + "union("+a1+","+a2+") = "+a1+"+ "+a2+" ; \n";
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

}