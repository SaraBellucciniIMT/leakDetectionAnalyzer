package pcrrlalgoelement;

import java.util.ArrayList;
import java.util.List;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.Operator;
import spec.mcrl2obj.Process;

/*
 * SIDE EFFECTS on mcrl2 specification
 */
public class CHOICE extends AbstractParaout {

	private Process dad;
	private Process child;
	private Process tpar1;
	private Process tpar2;
	// it contains t1.t2, t3.t4 , etc...
	private List<String> choicerip;

	@Override
	public void interpreter(Parout parout, Process dad, Process child) {
		this.dad = dad;
		this.child = child;
		String firstchoicename = constructFirstChoice(parout);
		List<String> followingchoicename = followingChoice(parout);
		followingchoicename.add(firstchoicename);
		dad.setOpertor(Operator.PARALLEL);
		dad.setChild(followingchoicename);
	}

	private String constructFirstChoice(Parout parout) {
		List<String> mainnewchildren = new ArrayList<String>();
		for (int i = 0; i < dad.getLength(); i++) {
			Action t1 = Action.setTemporaryAction();
			Action t2 = Action.setTemporaryAction();
			List<String> newchildren = new ArrayList<String>();
			Process newprocess ;
			if (dad.getChildName(i).equals(child.getName())) {
				tpar1 = new Process(t1);
				tpar2 = new Process(t2);
				newchildren = t1Processt2(tpar1.getName(), child.getChildName(0), tpar2.getName());
				newprocess = new Process(Operator.DOT, newchildren);
				newprocess.addInsideDef(tpar1);
				newprocess.addInsideDef(tpar2);
			} else {
				Process pt1 = new Process(t1);
				Process pt2 = new Process(t2);
				//-------- t1.t2 ---------
				List<String> childseqtmp = new ArrayList<String>();
				childseqtmp.add(pt1.getName());
				childseqtmp.add(pt2.getName());
				Process seqtemp = new Process(Operator.DOT, childseqtmp);
				//-----------------------
				parout.addProcess(seqtemp);
				choicerip.add(seqtemp.getName());
				newchildren = t1Processt2(pt1.getName(), dad.getChildName(i), pt2.getName());
				newprocess = new Process(Operator.DOT, newchildren);
				newprocess.addInsideDef(pt1);
				newprocess.addInsideDef(pt2);
			}
			 
			parout.addProcess(newprocess);
			mainnewchildren.add(newprocess.getName());
			communicationFunctionUpdateSet(parout, t1, t1, Action.setTemporaryAction());
			communicationFunctionUpdateSet(parout, t2, t2, Action.setTemporaryAction());
		}
		Process firstchoice = new Process(Operator.PLUS, mainnewchildren);
		parout.addProcess(firstchoice);
		return firstchoice.getName();
	}

	private List<String> followingChoice(Parout parout) {
		List<String> mainnewchild = new ArrayList<String>();
		for (int i = 1; i < child.getLength(); i++) {
			Process subpar = new Process(Operator.DOT,
					t1Processt2(tpar1.getName(), child.getChildName(i), tpar2.getName()));
			subpar.addInsideDef(tpar1);
			subpar.addInsideDef(tpar2);
			parout.addProcess(subpar);
			List<String> temp = new ArrayList<String>(choicerip);
			temp.add(subpar.getName());
			Process subchoice = new Process(Operator.PLUS,temp);
			parout.addProcess(subchoice);
			mainnewchild.add(subchoice.getName());
		}
		return mainnewchild;
	}
}
