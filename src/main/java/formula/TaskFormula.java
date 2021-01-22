package formula;

import java.util.HashSet;
import java.util.Set;
import rpstTest.Utils;
import sort.Data;
import sort.ISort;
import spec.mcrl2obj.Processes.TaskProcess;

public class TaskFormula extends TextInterpreterFormula {

	protected static String generateTaskFormula(TaskProcess task, Set<Data> data,String openf,String closef) {
		String s = "";
		if (task == null || task.getDimActionMemory()< data.size()) {
			return "-1";
		}else {
			s = openf;
			Set<String> parameterplu = new HashSet<String>();
			if (data.size() < task.getDimActionMemory()) {
				parameterplu = generatePar(task.getDimActionMemory() - data.size());
				s = s + "exists ";
				int i = 0;
				for (String par : parameterplu) {
					s = s + par;
					if (i != parameterplu.size() - 1)
						s = s + ",";
					i++;
				}
				s = s + ":"+Data.nameSort()+".";
			}
			s = s + task.getAction().getId() + "({";
			if (!parameterplu.isEmpty()) {
				for(String par : parameterplu)
					s = s + par +",";
			}
			s+= Utils.organizeParameterAsString(data.toArray(new ISort[data.size()]));
			s = s + "})" + closef;
		}
		return s;
	}

	
	
}
