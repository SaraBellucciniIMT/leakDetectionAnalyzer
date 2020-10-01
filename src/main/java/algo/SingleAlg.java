package algo;

import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jbpt.algo.tree.rpst.RPST;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.bpmn.BpmnControlFlow;

import algo.interpreter.Tmcrl;
import io.ExploitedRPST;
import spec.ISpec;

/*
 * TO-DO: generate specification for a single model 
 * 
 */
public class SingleAlg extends AbstractTranslationAlg{

	public SingleAlg(Bpmn<BpmnControlFlow<FlowNode>, FlowNode> bpmn) {
		
	}
	/*
	 * BPMN -> mCRL2 specification without data
	 */
	protected static Tmcrl analyzeControlFlow() {
		return new Tmcrl(new ExploitedRPST(new RPST<ControlFlow<FlowNode>, FlowNode>(bpmn)), bpmn.getName(),
				bpmn.getId());
	}
	
	@Override
	public ISpec getSpec() {
		
	}

	@Override
	protected void analyzeData() {
		// TODO Auto-generated method stub
		
	}

	


	

}
