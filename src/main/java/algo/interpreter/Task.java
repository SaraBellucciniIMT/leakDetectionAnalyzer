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
	
		Action a = new Action(node.getCurrentNode().getId());
		a.setId(node.getCurrentNode().getId());
		a.setSecondName(node.getCurrentNode().getName());
		if(node.getCurrentNode().getPet()!= null)
			a.setPet(node.getCurrentNode().getPet());
		TaskProcess tp = new TaskProcess(a, node.getCurrentNode());
		node.addProcess(tp);
		node.addAction(a);
		return tp;
	}

	
}
