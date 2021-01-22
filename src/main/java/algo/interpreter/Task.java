package algo.interpreter;

import io.ExtendedNode;
import spec.mcrl2obj.TaskAction;
import spec.mcrl2obj.Processes.AbstractProcess;
import spec.mcrl2obj.Processes.TaskProcess;

/**
 * This is the Task class that generates from a node an abstract process that
 * represents a task element of the bpmn model. It contains an action that has
 * as name the id of the bpmn model representing the task
 * 
 * @see #interpreter(ExtendedNode)
 * @author S. Belluccini
 *
 */
public class Task implements ITProcess {

	@Override
	public AbstractProcess interpreter(ExtendedNode node) {
		TaskAction a;
		if (node.getPet() != null)
			a = new TaskAction(node.getName(), node.getId(), node.getPet());
		else
			a = new TaskAction(node.getName(), node.getId());

		TaskProcess tp = new TaskProcess(a);
		return tp;
	}

}
