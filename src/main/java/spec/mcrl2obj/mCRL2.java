package spec.mcrl2obj;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import spec.ISpec;

/*
 * Class that define a mcrl2 spec object 
 */
public class mCRL2 implements ISpec {

	Sort sort;
	Set<Action> actions;
	Set<Action> allow;
	Set<CommunicationFunction> comm;
	Set<Action> hide;
	Set<AbstractProcess> processes;
	Set<String> initSet;
	private static int id =0;
	public mCRL2() {
		this.actions = new HashSet<Action>();
		this.allow = new HashSet<Action>();
		this.comm = new HashSet<CommunicationFunction>();
		this.hide = new HashSet<Action>();
		this.processes = new HashSet<AbstractProcess>();
		this.initSet = new HashSet<String>();
		
	}

	public Sort getSort() {
		return sort;
	}

	public void addCommunicaitonFunction(CommunicationFunction f) {
		this.comm.add(f);
	}

	public void setSort(Sort sort) {
		this.sort = sort;
	}

	public Set<Action> getActions() {
		return actions;
	}

	public void addAction(Action action) {
		boolean t = false;
		for (Action a : actions) {
			if (a.getName().equals(action.getName()) && a.nparameter() == action.nparameter())
				t = true;
		}
		if (!t)
			this.actions.add(action);
	}

	/*
	 * public void tauimporvement() {
	 * 
	 * for(AbstractProcess p : processes) { if(p.) } }
	 */
	public void addActions(Set<Action> actions) {
		this.actions.addAll(actions);
	}

	public Set<Action> getAllow() {
		return allow;
	}

	public void addAllow(Action allow) {
		this.allow.add(allow);
	}

	public void addAllow(Set<Action> allow) {
		this.allow.addAll(allow);
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

	public void addHide(Action hide) {
		this.hide.add(hide);
	}

	public Set<AbstractProcess> getProcesses() {
		return processes;
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

	public void addInitSet(String initSet) {
		this.initSet.add(initSet);
	}

	public void mcrl22file(String f) {
		File file = new File(f + ".mcrl2");
		if (!file.exists()) {
			try (BufferedWriter output = new BufferedWriter(new FileWriter(file))) {
				output.write("sort" + sort.getName() + "=" + "struct");
				int i = 0;
				for (String s : sort.getTypes()) {
					output.write(s);
					if (i != sort.getTypes().size())
						output.write("|");
					else
						output.write(";\n");
				}
				output.write("act");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	
	@Override
	public String toString() {
		;
		String s = sort.toString() + ";\n";
		s = s + "act" + "\n";
		Map<Integer, Set<Action>> classact = classifyaction();
		for (Entry<Integer, Set<Action>> entry : classact.entrySet()) {
			int i = 0;
			for (Action a : entry.getValue()) {
				s = s + a.getName();
				if (i != entry.getValue().size() - 1)
					s = s + ",";
				i++;
			}
			if (entry.getKey() != 0) {
				s = s + " : " + sort.getName();
				for (int j = 0; j < entry.getKey() - 1; j++) {
					s = s + "#" + sort.getName();
				}
			}
			s = s + ";\n";
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

	private Map<Integer, Set<Action>> classifyaction() {
		// n of parameters - action
		Map<Integer, Set<Action>> classact = new HashMap<Integer, Set<Action>>();
		for (Action a : actions) {
			if (classact.containsKey(a.nparameter())) {
				classact.get(a.nparameter()).add(a);
			} else {
				Set<Action> actions = new HashSet<Action>();
				actions.add(a);
				classact.put(a.nparameter(), actions);
			}
		}
		return classact;
	}

	public void taureduction() {
		List<String> processtoremove = new ArrayList<String>();
		for (AbstractProcess p : processes) {
			if (p.getClass().equals(Process.class) && ((Process) p).hasChild()) {
				Process process = (Process) p;
				List<String> newchilds = new ArrayList<String>();
				for (int i = 0; i < process.getLength(); i++) {
					AbstractProcess child;
					if ((child = identifyAbstractProcess(process.getChildName(i))).getClass().equals(Process.class)) {
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
		processes = newprocess;
	}

	public AbstractProcess identifyAbstractProcess(String name) {
		for (AbstractProcess p : processes) {
			if (p.getName().equals(name))
				return p;
		}
		return null;
	}

	public void removeProcess(String name) {
		for(AbstractProcess ap : this.processes) {
			if(ap.getName().equals(name)) {
				this.processes.remove(ap);
				System.out.println(name + "REMOVED");
				break;
			}
		}
		
	}

	
	public Process identifyProcess(String name) {
		for (AbstractProcess p : processes) {
			if (p.getName().equals(name))
				return (Process) p;
		}
		return null;
	}

	public void toFile(String fileName) {
		File file = new File(fileName + ".mcrl2");
		if (!file.exists()) {
			try (BufferedWriter output = new BufferedWriter(new FileWriter(file))) {
				output.write(toString());
			} catch (Exception e) {
				// TODO: handle exception
			}
		} else
			toFile(fileName+"(" +(id++) +")");
		System.out.println(fileName + " GENERATED");

	}
}