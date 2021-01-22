package pcrrlalgoelement;

import spec.mcrl2obj.Action;
import spec.mcrl2obj.MCRL2;
import spec.mcrl2obj.Operator;
import spec.mcrl2obj.Processes.AbstractProcess;
import spec.mcrl2obj.Processes.Process;

/*
 * SIDE EFFECTS on mcrl2 specification
 */
public class Choice extends AbstractParaout {

	@Override
	public AbstractProcess interpreter(Process process) {
		
		Process p = new Process(Operator.PLUS);
		for (int i = 0; i < process.getSize(); i++)
			p.addChildAtPosition(i, Parout.Tp(process.getChildAtPosition(i)));

		if (!AbstractParaout.hasParallel(p))
			return p;

		// Index of the block with the parallel operator
		int indexParBlock = -1;
		for (int i = 0; i < p.getSize(); i++) {
			if (p.getChildAtPosition(i).getClass().equals(Process.class) && ((Process)p.getChildAtPosition(i)).getOperator().equals(Operator.PARALLEL)) {
				indexParBlock = i;
				break;
			}
		}

		Process parallelProcess = ((Process)p.getChildAtPosition(indexParBlock));
		// Construct the left side
		Process leftSide = new Process(Operator.PLUS);

		Process[] otherSide = new Process[p.getSize() - 1];
		int j = 0;
		for (int i = 0; i < p.getSize(); i++) {
			if (i != indexParBlock) {
				// ti.Pi.ti
				Action t = MCRL2.getTemporaryAction();
				leftSide.addChildAtPosition(j, new Process(Operator.DOT, t,p.getChildAtPosition(i),t));
				// ti.ti
				communicationFunctionUpdateSet(t, parallelProcess.getSize());
				otherSide[j] = new Process(Operator.DOT, t,t);
				j++;
			}
		}

		// t.Q1.t
		Action t = MCRL2.getTemporaryAction();
		communicationFunctionUpdateSet(t, parallelProcess.getSize());
		leftSide.addChildAtPosition(leftSide.getSize(), new Process(Operator.DOT, t,parallelProcess.getChildAtPosition(0),t));
		leftSide = (Process) new Choice().interpreter(leftSide);

		// Put all togheter in a unique parallel block
		Process parallelBlock = new Process(Operator.PARALLEL);
		// Add the left side
		parallelBlock.addChildAtPosition(0, leftSide);
		// Construct and add the other sides
		for (int i = 1; i < parallelProcess.getSize(); i++) {
			// t.Qi.t
			parallelBlock.addChildAtPosition(i, new Process(Operator.DOT, t,parallelProcess.getChildAtPosition(j),t));
		}
		return parallelBlock;
	}
}
