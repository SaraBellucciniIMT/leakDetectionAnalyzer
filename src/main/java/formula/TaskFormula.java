package formula;

import java.util.HashSet;
import java.util.Set;

import spec.mcrl2obj.TaskProcess;
import spec.mcrl2obj.mCRL2;

public class TaskFormula extends TextInterpreterFormula {

	protected static String generateTaskFormula(mCRL2 mcrl2, String idtaskname, Set<String> data) {
		TaskProcess task = identifyIdTaskFormula(mcrl2,idtaskname);
		
		String s = "";
		if (task == null || task.getAction().nparameter() < data.size()) {
			return "-1";
		}else {
			s = openpossibilityformula;
			Set<String> parameterplu = new HashSet<String>();
			if (data.size() < task.getAction().nparameter()) {
				parameterplu = generatePar(task.getAction().nparameter() - data.size());
				s = s + "exists ";
				int i = 0;
				for (String par : parameterplu) {
					s = s + par;
					if (i != parameterplu.size() - 1)
						s = s + ",";
					i++;
				}
				s = s + ":Data.";
			}

			//s = s + "(";
			if (!parameterplu.isEmpty())
				data.addAll(parameterplu);
			s = s + task.getAction().getName() + "({";
			int i=0;
			for(String d : data) {
				s = s + d;
				if(i != data.size()-1)
					s =s + ",";
				i++;
			}
			s = s + "})" + closepossibilityformula;
		}
		return s;
	}

	
	
}
