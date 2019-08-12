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
	private List<String> choicerip = new ArrayList<String>();

	@Override
	public void interpreter(Parout parout, Process dad, Process child) {
		this.dad = dad;
		this.child = child;
		String firstchoicename = constructFirstChoice(parout);
		
		List<String> followingchoicename = followingChoice(parout);
		followingchoicename.add(firstchoicename);
		dad.setOpertor(Operator.PARALLEL);
		dad.setChild(followingchoicename);
		parout.removeProcess(child.getName());
	}

	private String constructFirstChoice(Parout parout) {
		Process firstchoice = new Process(Operator.PLUS);
		for (int i = 0; i < dad.getLength(); i++) {
			Action t1 = Action.setTemporaryAction();
			Action t2 = Action.setTemporaryAction();
			Process newprocess ;
			if (dad.getChildName(i).equals(child.getName())) {
				tpar1 = new Process(t1);
				tpar2 = new Process(t2);
				newprocess = new Process(Operator.DOT, tpar1.getName(), child.getChildName(0), tpar2.getName());
				newprocess.addInsideDef(tpar1,tpar2);
				
			} else {
				Process pt1 = new Process(t1);
				Process pt2 = new Process(t2);
				//-------- t1.t2 --------- useful later
				Process seqtemp = new Process(Operator.DOT, pt1.getName(),pt2.getName());
				seqtemp.addInsideDef(pt1,pt2);
				//-----------------------
				parout.addProcess(seqtemp);
				choicerip.add(seqtemp.getName());
				newprocess = new Process(Operator.DOT, pt1.getName(), dad.getChildName(i), pt2.getName());
				newprocess.addInsideDef(pt1,pt2);
				if(dad.getInsideDef(dad.getChildName(i))!= null)
					newprocess.addInsideDef(dad.getInsideDef(dad.getChildName(i)));
			}
			 
			parout.addProcess(newprocess);
			firstchoice.addChild(newprocess.getName());
			communicationFunctionUpdateSet(parout, Action.setTemporaryAction(), t1, child.getLength());
			communicationFunctionUpdateSet(parout, Action.setTemporaryAction(), t2, child.getLength());
		}
		parout.addProcess(firstchoice);
		return firstchoice.getName();
	}

	private List<String> followingChoice(Parout parout) {
		List<String> mainnewchild = new ArrayList<String>();
		for (int i = 1; i < child.getLength(); i++) {
			Process subpar = new Process(Operator.DOT,
					tpar1.getName(), child.getChildName(i), tpar2.getName());
			subpar.addInsideDef(tpar1,tpar2);
			parout.addProcess(subpar);
			List<String> temp = new ArrayList<String>(choicerip);
			temp.add(subpar.getName());
			Process subchoice = new Process(Operator.PLUS,temp.toArray(new String[] {}));
			parout.addProcess(subchoice);
			mainnewchild.add(subchoice.getName());
		}
		return mainnewchild;
	}
}
