/**
 * 
 */
package pcrrlalgoelement;

import spec.mcrl2obj.Action;
import spec.mcrl2obj.MCRL2;
import spec.mcrl2obj.Operator;
import spec.mcrl2obj.Processes.AbstractProcess;
import spec.mcrl2obj.Processes.Process;

/**
 * It apply the tp_seq function to the abstract process
 * @author S. Belluccini
 *
 */
public class Sequence extends AbstractParaout {

	@Override
	public AbstractProcess interpreter(Process process) {

		Process p = new Process(Operator.DOT);
		for (int i = 0; i < process.getSize(); i++)
			p.addChild(Parout.Tp(process.getChildAtPosition(i)));

		if (!AbstractParaout.hasParallel(p))
			return p;

		int indexParBlock = -1;
		for (int i = 0; i < p.getSize(); i++) {
			AbstractProcess child = p.getChildAtPosition(i);
			if (child.getClass().equals(Process.class) && ((Process) child).getOperator().equals(Operator.PARALLEL)) {
				indexParBlock = i;
				break;
			}
		}
		Process parallelProcess = ((Process)process.getChildAtPosition(indexParBlock));
		// Generating the left side part of the new parallel block, first remove the
		// parallel block from the sequence
		Process leftSideSequence = p.removeChildAtPosition(indexParBlock);

		// Constructing t and Q
		AbstractProcess parBlockFirstBlock = parallelProcess.getChildAtPosition(0);
		Action t = MCRL2.getTemporaryAction();
		// Adding t.Q.t
		int j = indexParBlock;
		leftSideSequence.addChildAtPosition(j, t);
		leftSideSequence.addChildAtPosition(++j, parBlockFirstBlock);
		leftSideSequence.addChildAtPosition(++j, t);
		// Add t and t' to the comm, allow and hide sets
		communicationFunctionUpdateSet(t, parallelProcess.getSize());
		
		// Repeat the procedure over the new sequence to check if there are other
		// parallel blocks
		leftSideSequence = (Process) new Sequence().interpreter(leftSideSequence);

		// Put all togheter in a unique parallel block
		Process parallelBlock = new Process(Operator.PARALLEL);

		parallelBlock.addChildAtPosition(0,leftSideSequence);
		for (int i = 1; i < parallelProcess.getSize(); i++) {
			// t.Qi.t
			Process seq = new Process(Operator.DOT, t,parallelProcess.getChildAtPosition(i),t);
			parallelBlock.addChildAtPosition(i, seq);
		}
		return parallelBlock;
	}

}
