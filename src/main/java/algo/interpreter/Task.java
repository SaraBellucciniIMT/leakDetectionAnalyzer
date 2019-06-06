package algo.interpreter;


import spec.mcrl2obj.AbstractProcess;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.TaskProcess;

/*
 * TO-DO: ricrevere codice Task
 */
public class Task implements ITProcess{

	@Override
	public AbstractProcess interpreter(Tmcrl node) {
		// TODO Auto-generated method stub
		Action a = new Action(node.getCurrentNode().getName());
		TaskProcess tp = new TaskProcess(a, node.getCurrentNode());
		node.addProcess(tp);
		node.addAction(a);
		return tp;
	}

	
}
