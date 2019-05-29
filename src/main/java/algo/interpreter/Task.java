package algo.interpreter;

import algo.TaskProcess;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.Process;

/*
 * TO-DO: ricrevere codice Task
 */
public class Task implements ITProcess{

	@Override
	public Process interpreter(Tmcrl node) {
		// TODO Auto-generated method stub
		TaskProcess tp = new TaskProcess(new Action(node.getCurrentNode().getName()), node.getCurrentNode());
		node.addTaskProcess(tp);
		return tp;
	}

	
}
