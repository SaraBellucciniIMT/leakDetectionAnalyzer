package algo.interpreter;

import io.ExtendedNode;
import spec.mcrl2obj.Processes.AbstractProcess;
import spec.mcrl2obj.Processes.LoopProcess;

public class LoopBond implements ITProcess{


	@Override
	public AbstractProcess interpreter(ExtendedNode node) {
		LoopProcess l = new LoopProcess(Tmcrl.applyT(node));
		return l;
	}

}
