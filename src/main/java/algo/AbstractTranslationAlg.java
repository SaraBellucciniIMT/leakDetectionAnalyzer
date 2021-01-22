
package algo;

import org.jbpt.algo.tree.rpst.RPST;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.bpmn.BpmnControlFlow;

import algo.interpreter.Tmcrl;
import io.ExploitedRPST;
import spec.mcrl2obj.Processes.ParticipantProcess;

/**
 * Abstract class that defines how to compute the control flow o a bpmn model
 * for translation algorithms
 * 
 * @author S. Belluccini
 *
 */
public abstract class AbstractTranslationAlg implements ITranslationAlg {

	/**
	 * First compute the extended RPST of a bpmn model and then apply the T function
	 * (translation function) over it to obtain the control flow of model specified
	 * using the mCRL2 language. Look at Section 3.2 of the Verification of
	 * privacy-enahnced collaboration for more details.
	 * 
	 * @param bpmn the model to analyze
	 * @return an AbstractProcess representing the control-flow specification of this participant
	 */
	protected static ParticipantProcess analyzeControlFlow(Bpmn<BpmnControlFlow<FlowNode>, FlowNode> bpmn) {
		return Tmcrl.computeTmcrl(new ExploitedRPST(new RPST<ControlFlow<FlowNode>, FlowNode>(bpmn)), bpmn.getId(),
				bpmn.getName());
	}

}
