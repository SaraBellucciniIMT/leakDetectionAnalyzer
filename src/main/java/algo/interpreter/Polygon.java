/**
 * 
 */
package algo.interpreter;

import java.util.List;

import io.ExtendedNode;
import spec.mcrl2obj.AbstractProcess;
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
	public AbstractProcess interpreter(Tmcrl node) {
			String[] childList = new String[successors.size()];
			for (int i = 0; i < successors.size(); i++) {
				AbstractProcess process = node.applyT(successors.get(i));
				node.addProcess(process);
				childList[i]= process.getName();
					}
			return new Process(Operator.DOT,childList);
		
	}

}
