package formula;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.iterators.PermutationIterator;

import spec.mcrl2obj.TaskProcess;
import spec.mcrl2obj.mCRL2;

public class TaskFormula extends TextInterpreterFormula {

	private static String parameter = "p";
	private static int id = 0;
	protected static String generateTaskFormula(mCRL2 mcrl2, String taskname, Set<String> data) {
		TaskProcess task = null;
		for (TaskProcess tp : mcrl2.getTaskProcessesInsideProcesses()) {
			if (tp.getAction().getName().equals(taskname))
				task = tp;
		}
		String s = "";
		if (task == null || task.getAction().nparameter() < data.size()) 
			return s;
		else {
			s = openpossibilityformula ;
			Set<String> parameterplu = new HashSet<String>();
			if(data.size() < task.getAction().nparameter() ){
				parameterplu = generatePar(task.getAction().nparameter()-data.size());
				s = s+ "exists ";
				int i=0;
				for(String par : parameterplu) {
					s = s + par;
					if(i != parameterplu.size()-1)
						s = s +",";
					i++;
				}
				s = s + ":Data.";
			}
				
			s = s+ "(";
			if(!parameterplu.isEmpty())
				data.addAll(parameterplu);
			PermutationIterator<String> per = new PermutationIterator<String>(data);
			while (per.hasNext()) {
				List<String> permutation = per.next();
				s = s + task.getAction().getName() + "(";
				for (int i = 0; i < permutation.size() - 1; i++)
					s = s + permutation.get(i) + ",";
				s = s + permutation.get(permutation.size() - 1) + ")";
				if (per.hasNext())
					s = s + "||";
			}
			s = s + ")"+ closepossibilityformula;
		}
		return s;
	}

	private static Set<String> generatePar(int n) {
		Set<String> s = new HashSet<String>();
		while(n!=0) {
			s.add(parameter + id++);
			n--;
		}
		return s;
	}
}
