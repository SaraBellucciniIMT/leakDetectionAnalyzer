package algo.interpreter;

import spec.mcrl2obj.Action;
import spec.mcrl2obj.Process;
import spec.mcrl2obj.TaskProcess;

public class CatchingEvent implements ITProcess{

	@Override
	public Process interpreter(Tmcrl node) {
		// TODO Auto-generated method stub

		Action a = new Action(node.getCurrentNode().getId());
		a.setId(node.getCurrentNode().getId());
		TaskProcess tp = new TaskProcess(a, node.getCurrentNode());
		node.addProcess(tp);
		node.addAction(a);
		return new Process(new Action());
	}

}
