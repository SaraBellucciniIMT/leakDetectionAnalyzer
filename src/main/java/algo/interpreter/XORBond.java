package algo.interpreter;

import java.util.Collection;

import io.ExtendedNode;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.Operator;
import spec.mcrl2obj.Processes.AbstractProcess;
import spec.mcrl2obj.Processes.Process;

/**
 * This is the XORBond class that represents XORBOND as a external choice
 * process (tau.P1 + tau.P2 + ... + tau.PN) if the elements inside the node are >1
 * otherwise it returns the representation on the single node
 * 
 * @see #interpreter(Tmcrl)
 * @author S. Belluccini
 *
 */
public class XORBond implements ITProcess {

	private Collection<ExtendedNode> successors;

	public XORBond(Collection<ExtendedNode> successors) {
		this.successors = successors;
	}

	@Override
	public AbstractProcess interpreter(ExtendedNode node) {
		AbstractProcess[] childlist = new AbstractProcess[successors.size()];
		if (childlist.length == 1)
			return Tmcrl.applyT(successors.stream().findAny().get());
		int i = 0;
		for (ExtendedNode n : successors) {
			AbstractProcess process = Tmcrl.applyT(n);
			AbstractProcess sequencep = new Process(Operator.DOT, new Action(),process);
			childlist[i] = sequencep;
			i++;
		}

		return new Process(Operator.PLUS, childlist);
	}
}
