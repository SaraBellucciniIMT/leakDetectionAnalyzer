package algo.interpreter;

import io.ExtendedNode;
import spec.mcrl2obj.TaskAction;
import spec.mcrl2obj.Processes.AbstractProcess;
import spec.mcrl2obj.Processes.TaskProcess;

/**
 * This is the intermediate catching event class that return an abstract process
 * that has an action representing the intermediate catching message of the bpmn
 * model
 * 
 * @see #interpreter(ExtendedNode)
 * @author S. Belluccini
 *
 */

public class CatchingEvent implements ITProcess {

	@Override
	public AbstractProcess interpreter(ExtendedNode node) {
		TaskAction a;
		if(node.getName()!= null && !node.getName().equals(""))
			a = new TaskAction(node.getName(),node.getId());
		else
			a = new TaskAction(node.getId(), node.getId());
		
		TaskProcess tp = new TaskProcess(a);
		return tp;
	}

}
