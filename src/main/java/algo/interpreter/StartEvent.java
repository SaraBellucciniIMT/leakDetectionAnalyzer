package algo.interpreter;

import io.ExtendedNode;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.Processes.AbstractProcess;

/**
 * This is the Start Event class that given a node generates a process with an empty action
 * 
 * @see StartEvent#interpreter(Tmcrl)
 * @author S. Belluccini
 *
 */
public class StartEvent implements ITProcess {
	
	@Override
	public AbstractProcess interpreter(ExtendedNode node) {
		return new Action();
	}

}
