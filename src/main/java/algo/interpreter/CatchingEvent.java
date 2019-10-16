package algo.interpreter;

import spec.mcrl2obj.AbstractProcess;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.Process;
import spec.mcrl2obj.TaskProcess;

public class CatchingEvent implements ITProcess{

	@Override
	//Before was returing a process
	public AbstractProcess interpreter(Tmcrl node) {
		// TODO Auto-generated method stub

		Action a = new Action(node.getCurrentNode().getId());
		a.setId(node.getCurrentNode().getId());
		TaskProcess tp = new TaskProcess(a, node.getCurrentNode());
		node.addProcess(tp);
		node.addAction(a);
		return tp;
	
	}

}
