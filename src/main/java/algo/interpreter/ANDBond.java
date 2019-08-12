package algo.interpreter;

import java.util.Collection;

import algo.interpreter.Tmcrl;
import io.ExtendedNode;
import spec.mcrl2obj.AbstractProcess;
import spec.mcrl2obj.Operator;
import spec.mcrl2obj.Process;

public class ANDBond implements ITProcess {

private Collection<ExtendedNode> successors;
	
	
	public ANDBond(Collection<ExtendedNode> successors) {
		this.successors = successors;
	}
	
	@Override
	public AbstractProcess interpreter(Tmcrl node) {
		String[] childlist = new String[successors.size()];
		//This means that remove the bound the polygon is reprenset another element 
		if (childlist.length == 1)
			return node.applyT(successors.stream().findAny().get());
		int i=0;
		for(ExtendedNode n : successors) {
			AbstractProcess process = node.applyT(n);
			node.addProcess(process);
			childlist[i]=process.getName();
			i++;
		}
		return new Process(Operator.PARALLEL, childlist);
	}

	

}
