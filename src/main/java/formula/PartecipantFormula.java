package formula;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import spec.mcrl2obj.AbstractProcess;
import spec.mcrl2obj.Operator;
import spec.mcrl2obj.Process;
import spec.mcrl2obj.TaskProcess;
import spec.mcrl2obj.mCRL2;

/*
 * 1° understand who are the of parteipant name
 */
public class PartecipantFormula extends TextInterpreterFormula {

	protected static String generatePartecipantFormula(mCRL2 mcrl2, String partecipantname, Set<String> data) {
		List<String> childs = new ArrayList<String>();
		Process partecipant = mcrl2.getPartcipant(partecipantname);
		childs = childTaskProcess(partecipant, mcrl2,childs);
		String formula = "";
		for(int i=0; i < childs.size(); i ++) {	
			String s = TaskFormula.generateTaskFormula(mcrl2, childs.get(i), data);
			if(!s.isEmpty()) {
				if(!formula.isEmpty())
					formula = formula + Operator.PARALLEL.getValue() ; 
				formula = formula + s;
			}
		}	
		return formula;
	}
	
	private static List<String> childTaskProcess(Process partecipant, mCRL2 mcrl2,List<String> childs) {
		if (!partecipant.hasChild())
			return childs;
		for (int i = 0; i < partecipant.getLength(); i++) {
			AbstractProcess child = mcrl2.identifyAbstractProcess(partecipant.getChildName(i));
			if(child == null)
				continue;
			System.out.println(child);
			if (child.getClass().equals(TaskProcess.class))
				childs.add(((TaskProcess) child).getAction().getName());
			else
				childTaskProcess(((Process) child), mcrl2,childs);
		}
		return childs;
	}

}
