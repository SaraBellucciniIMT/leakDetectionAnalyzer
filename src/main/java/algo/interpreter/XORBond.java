package algo.interpreter;

import java.util.Collection;

import io.ExtendedNode;
import spec.mcrl2obj.Operator;
import spec.mcrl2obj.Process;

public class XORBond implements ITProcess{

private Collection<ExtendedNode> successors;
	
	
	public XORBond(Collection<ExtendedNode> successors) {
		this.successors = successors;
	}
	
	@Override
	public Process interpreter(Tmcrl node) {
		Process[] p = new Process[successors.size()];

		int i=0;
		for(ExtendedNode n : successors) {
			p[i] = node.applyT(n);
			i++;
		}
		return new Process(Operator.PLUS, p);
	}
}
