/**
 * 
 */
package pcrrlalgoelement;

import java.util.ArrayList;
import java.util.List;

import spec.mcrl2obj.Action;
import spec.mcrl2obj.Operator;
import spec.mcrl2obj.Process;

/**
 * @author sara
 *
 */
public class Sequence extends AbstractParaout {

	private Process dad;
	private Process child;
	private Process t1;
	private Process t2;

	@Override
	public void interpreter(Parout parout, Process dad, Process child) {
		this.dad = dad;
		this.child = child;
		String firstsequencename = constructfirstsequence(parout);
		List<String> newchildren = followingSequence(parout);
		newchildren.add(0, firstsequencename);
		// ----This is the new Operator---
		dad.setOpertor(Operator.PARALLEL);
		dad.setChild(newchildren);

		parout.removeProcess(child.getName());
		System.out.println(dad.toString());

	}

	private String constructfirstsequence(Parout parout) {
		// Construct the first sequence
		List<String> newchild = new ArrayList<String>(dad.getLength() + 2);
		for (int i = 0; i < dad.getLength(); i++) {
			if (!dad.getChildName(i).equals(child.getName()))
				newchild.add(dad.getChildName(i));
			else {
				this.t1 = new Process(Action.setTemporaryAction());
				this.t2 = new Process(Action.setTemporaryAction());
				newchild.addAll(t1Processt2(t1.getName(), child.getChildName(0), t2.getName()));
				communicationFunctionUpdateSet(parout, t1.getAction(), t1.getAction(), Action.setTemporaryAction());
				communicationFunctionUpdateSet(parout, t2.getAction(), t2.getAction(), Action.setTemporaryAction());
			}
		}
		Process firstsequence = new Process(Operator.DOT, newchild);
		firstsequence.addInsideDef(t1);
		firstsequence.addInsideDef(t2);
		parout.addProcess(firstsequence);
		return firstsequence.getName();
	}

	private List<String> followingSequence(Parout parout) {
		List<String> followingsequence = new ArrayList<String>();
		for (int i = 1; i < child.getLength(); i++) {
			List<String> newchilds = t1Processt2(t1.getName(), child.getChildName(i), t2.getName());
			Process p = new Process(Operator.DOT, newchilds);
			p.addInsideDef(t1);
			p.addInsideDef(t2);
			parout.addProcess(p);
			followingsequence.add(p.getName());
		}
		return followingsequence;
	}

}
