package algo.interpreter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
		List<String> childlist = new ArrayList<String>(successors.size());

		for(ExtendedNode n : successors) {
			AbstractProcess process = node.applyT(n);
			node.addProcess(process);
			childlist.add(process.getName());
		}
		
		return new Process(Operator.PLUS, childlist);
	}
}
