package algo.interpreter;

import java.util.Collection;

import io.ExtendedNode;
import spec.mcrl2obj.Operator;
import spec.mcrl2obj.Processes.AbstractProcess;
import spec.mcrl2obj.Processes.Process;

public class ANDBond implements ITProcess {

	private Collection<ExtendedNode> successors;

	public ANDBond(Collection<ExtendedNode> successors) {
		this.successors = successors;
	}

	@Override
	public AbstractProcess interpreter(ExtendedNode node) {
		AbstractProcess[] childlist = new AbstractProcess[successors.size()];
		// This means that remove the bound the polygon is reprenset another element
		if (childlist.length == 1)
			return Tmcrl.applyT(successors.stream().findAny().get());
		int i = 0;
		for (ExtendedNode n : successors) {
			AbstractProcess process = Tmcrl.applyT(n);
			childlist[i] = process;
			i++;
		}
		return new Process(Operator.PARALLEL, childlist);
	}

}
