/**
 * 
 */
package algo.interpreter;

import java.util.List;

import io.ExtendedNode;
import spec.mcrl2obj.Operator;
import spec.mcrl2obj.Processes.AbstractProcess;
import spec.mcrl2obj.Processes.Process;

/**
 * This is the Polygon class that represents a Polygon as a sequence process if
 * the elements inside the node are more than 1, otherwise it returns the representation
 * on the single node
 * 
 * @see #interpreter(ExtendedNode)
 * @author S. Belluccini
 *
 */
public class Polygon implements ITProcess {

	private List<ExtendedNode> successors;

	public Polygon(List<ExtendedNode> successors) {
		this.successors = successors;
	}

	@Override
	public AbstractProcess interpreter(ExtendedNode node) {
		AbstractProcess[] childList = new AbstractProcess[successors.size()];
		// If removing the boundaries of the bound node there is just one element, than
		// is not anymore a process but mCRL2 element itself
		if (childList.length == 1)
			return Tmcrl.applyT(successors.get(0));
		else {
			for (int i = 0; i < successors.size(); i++) {
				AbstractProcess process = Tmcrl.applyT(successors.get(i));
				childList[i] = process;
			}
			return new Process(Operator.DOT, childList);
		}
	}

}
