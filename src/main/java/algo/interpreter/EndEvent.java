package algo.interpreter;

import io.ExtendedNode;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.Processes.AbstractProcess;

/**
 * This is the end event class that given a node generates a process with an action that as isTau=true
 * 
 * @see #interpreter(Tmcrl)
 * @author S. Belluccini
 *
 */
public class EndEvent implements ITProcess{

	@Override
	public AbstractProcess interpreter(ExtendedNode node) {
			return new Action();
	}

}
