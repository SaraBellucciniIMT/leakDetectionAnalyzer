package algo;

import org.jbpt.pm.FlowNode;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.bpmn.BpmnControlFlow;

import algo.interpreter.Tmcrl;
import spec.ISpec;

/*
 * Continue the implementation of a single model
 */
public class SingleAlg extends AbstractTranslationAlg{

	Bpmn<BpmnControlFlow<FlowNode>, FlowNode> bpmn ;
	Tmcrl tmcrl;
	
	public SingleAlg(Bpmn<BpmnControlFlow<FlowNode>, FlowNode> bpmn) {
		// TODO Auto-generated constructor stub
		this.bpmn = bpmn;
	}
	
	
	@Override
	public ISpec getSpec() {
		this.tmcrl = analyzeControlFlow(bpmn);
		analyzeData();
		return null;
	}

	@Override
	protected void analyzeData() {
		// TODO Auto-generated method stub
		
	}

}
