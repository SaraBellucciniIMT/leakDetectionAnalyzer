package formula;

import java.util.HashSet;
import java.util.Set;

import rpstTest.Utils;
import sort.Data;
import sort.ISort;
import spec.mcrl2obj.Processes.ParticipantProcess;

/*
 * 1° understand who are the of parteipant name
 * 2° return null if the partecipant doesn't exist otherwise
 */
public class PartecipantFormula extends TextInterpreterFormula {

	/*protected static String generatePartecipantFormula(ParticipantProcess p, Set<Data> data) {
		Set<Data> analyzedata = new HashSet<Data>(data);
		String s = "";
		if (analyzedata.size() > p.getDimensionMemory()) {
			return null;
		}else {
			s = openpossibilityformula;
			Set<String> parameterplu = new HashSet<String>();
			if (analyzedata.size() < p.getDimensionMemory()) {
				parameterplu = generatePar(p.getDimensionMemory() - analyzedata.size());
				s = s + "exists ";
				int i = 0;
				for (String par : parameterplu) {
					s = s + par;
					if (i != parameterplu.size() - 1)
						s = s + ",";
					i++;
				}
				s = s + ":"+Data.class.getName()+".";
			}
			
			s = s + p.getMemoryAct().getId() + "({";
			if (!parameterplu.isEmpty()) {
				for(String par : parameterplu)
					s = s + par +",";
			}
			s += Utils.organizeParameterAsString(data.toArray(new ISort[data.size()]));
			s = s + "})" + closepossibilityformula;
		
		}
		return s;
	}*/

}
