package algo.interpreter;

import java.util.Collection;

import io.ExtendedNode;
import spec.mcrl2obj.AbstractProcess;
import spec.mcrl2obj.Operator;
import spec.mcrl2obj.Process;

public class XORBond implements ITProcess{

private Collection<ExtendedNode> successors;
	
	
	public XORBond(Collection<ExtendedNode> successors) {
		this.successors = successors;
	}
	
	@Override
	public AbstractProcess interpreter(Tmcrl node) {
		String[] childlist = new String[successors.size()];
		if (childlist.length == 1)
			return node.applyT(successors.stream().findAny().get());
		int i=0;
		for(ExtendedNode n : successors) {
			AbstractProcess process = node.applyT(n);
			node.addProcess(process);
			childlist[i]= process.getName();
			i++;
		}
		
		return new Process(Operator.PLUS, childlist);
	}
}
