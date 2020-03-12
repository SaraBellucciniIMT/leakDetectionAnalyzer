/**
 * 
 */
package algo;

import org.jbpt.algo.tree.rpst.RPST;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.bpmn.BpmnControlFlow;

import algo.interpreter.Tmcrl;
import io.ExploitedRPST;

/**
 * @author sara Abstract class that define how to compute the control flow of a
 *         bpmn model
 */
public abstract class AbstractTranslationAlg implements ITranslationAlg {

	protected Tmcrl analyzeControlFlow(Bpmn<BpmnControlFlow<FlowNode>, FlowNode> bpmn) {
		return new Tmcrl(new ExploitedRPST(new RPST<ControlFlow<FlowNode>, FlowNode>(bpmn)), bpmn.getName(),
				bpmn.getId());
	}

	protected abstract void analyzeData();
	public static int id_op;

}
