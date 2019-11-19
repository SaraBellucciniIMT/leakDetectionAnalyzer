package formula;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Triple;

import algo.AbstractTranslationAlg;
import io.pet.PET;
import spec.mcrl2obj.TaskProcess;
import spec.mcrl2obj.mCRL2;

public class TaskFormula extends TextInterpreterFormula {

	protected static String generateTaskFormula(mCRL2 mcrl2, TaskProcess task, Set<String> data,String openf,String closef) {
		//TaskProcess task = identifyIdTaskFormula(mcrl2,idtaskname);
		String s = "";
		if (task == null || task.getAction().nparameter() < data.size()) {
			return null;
		}else {
			s = openf;
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
				s = s + ":"+AbstractTranslationAlg.getSortEvalData().getName()+".";
			}

			s = s + task.getAction().getName() + "({";
			if (!parameterplu.isEmpty()) {
				for(String par : parameterplu)
					s = s + par +",";
			}
			int i=0;
			for(String d : data) {
				Triple<String,PET,Integer> triple =AbstractTranslationAlg.getSortEvalData().getTripleByName(d);
				if(triple.getMiddle()== null)
					s = s + "triple("+ triple.getLeft() + "," + false +"," + triple.getRight()+")";
				else
					s = s + "triple("+ triple.getLeft() + "," + true +"," + triple.getRight()+")";
				if(i != data.size()-1)
					s =s + ",";
				i++;
			}
			s = s + "})" + closef;
		}
		return s;
	}

	
	
}
