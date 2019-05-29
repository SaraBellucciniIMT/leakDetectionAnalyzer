/**
 * 
 */
package algo.interpreter;

import java.util.List;

import io.ExtendedNode;
import spec.mcrl2obj.Operator;
import spec.mcrl2obj.Process;

/**
 * @author sara
 *
 */
public class Polygon implements ITProcess {

	private List<ExtendedNode> successors;

	public Polygon(List<ExtendedNode> successors) {
		this.successors = successors;
	}

	@Override
	public Process interpreter(Tmcrl node) {
		if (successors.size() > 1) {
			Process[] p = new Process[successors.size()];
			for (int i = 0; i < successors.size(); i++) {
				p[i] = node.applyT(successors.get(i));
			}
			return new Process(Operator.DOT, p);
		}else {
			return new Task().interpreter(node);
		}
	}

}
