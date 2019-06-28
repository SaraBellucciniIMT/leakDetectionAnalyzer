package formula;

import java.util.HashSet;
import java.util.Set;

import spec.mcrl2obj.PartecipantProcess;
import spec.mcrl2obj.mCRL2;

/*
 * 1° understand who are the of parteipant name
 */
public class PartecipantFormula extends TextInterpreterFormula {

	protected static String generatePartecipantFormula(mCRL2 mcrl2, String partecipantname, Set<String> data) {
		PartecipantProcess partecipant = (PartecipantProcess) mcrl2.getPartcipant(partecipantname);
		String s = "";
		if (data.size() > partecipant.getDimensionMemory())
			return s;
		else {
			s = openpossibilityformula;
			Set<String> parameterplu = new HashSet<String>();
			if (data.size() < partecipant.getDimensionMemory()) {
				parameterplu = generatePar(partecipant.getDimensionMemory() - data.size());
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
			if (!parameterplu.isEmpty())
				data.addAll(parameterplu);
			s = s + partecipant.getActionMemory().getName() + "({";
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
