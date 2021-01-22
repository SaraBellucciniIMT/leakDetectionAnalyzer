package algo.interpreter;

import io.ExtendedNode;
import spec.mcrl2obj.Processes.AbstractProcess;

/**
 * This is the interface for the translation of Tmcrl nodes in Abstract process
 * 
 * @see #interpreter(Tmcrl)
 * 
 * @author Sara
 *
 */
public interface ITProcess {
	
	/**
	 * Returns the abstract process representing the node of the extended RPST
	 * @param node of the Extended RPST
	 * @return the abstract process representing the node of the extended RPST
	 */
	AbstractProcess interpreter(ExtendedNode node);

}
