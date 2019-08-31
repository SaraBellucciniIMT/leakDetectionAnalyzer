package formula;

import java.util.HashSet;
import java.util.Set;

import spec.mcrl2obj.PartecipantProcess;
import spec.mcrl2obj.mCRL2;

/*
 * 1° understand who are the of parteipant name
 * 2° return null if the partecipant doesn't exist otherwise
 */
public class PartecipantFormula extends TextInterpreterFormula {

	protected static String generatePartecipantFormula(mCRL2 mcrl2, String partecipantname, Set<String> data) {
		Set<String> analyzedata = new HashSet<String>(data);
		PartecipantProcess partecipant = (PartecipantProcess) mcrl2.getPartcipant(partecipantname);
		if(partecipant == null) {
			System.err.println("This partecipant doesn't exist");
			return null;
		}
		String s = "";
		if (analyzedata.size() > partecipant.getDimensionMemory()) {
			return "-1";
		}else {
			s = openpossibilityformula;
			Set<String> parameterplu = new HashSet<String>();
			if (analyzedata.size() < partecipant.getDimensionMemory()) {
				parameterplu = generatePar(partecipant.getDimensionMemory() - analyzedata.size());
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
				analyzedata.addAll(parameterplu);
			s = s + partecipant.getActionMemory().getName() + "({";
			int i=0;
			for(String d : analyzedata) {
				s = s + d;
				if(i != analyzedata.size()-1)
					s =s + ",";
				i++;
			}
			s = s + "})" + closepossibilityformula;
		
		}
		return s;
	}

}
