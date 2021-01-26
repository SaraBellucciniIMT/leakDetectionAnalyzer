package algo.interpreter;

import io.DotBPMNKeyW;
import io.ExtendedNode;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.TaskAction;
import spec.mcrl2obj.Processes.AbstractProcess;
import spec.mcrl2obj.Processes.TaskProcess;

/**
 * This is the Start Event class that given a node generates a process with an
 * empty action
 * 
 * @see #interpreter(ExtendedNode)
 * @author S. Belluccini
 *
 */
public class StartEvent implements ITProcess {

	@Override
	public AbstractProcess interpreter(ExtendedNode node) {
		TaskAction a = new TaskAction(node.getId(), node.getId());
		TaskProcess tp = new TaskProcess(a);
		tp.setBPMNElement(DotBPMNKeyW.STARTEVENT);
		return tp;
	}

}
