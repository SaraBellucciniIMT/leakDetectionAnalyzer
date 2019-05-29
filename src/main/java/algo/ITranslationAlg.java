package algo;

import java.util.Set;

import org.jbpt.pm.FlowNode;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.bpmn.BpmnControlFlow;

import spec.ISpec;

public interface ITranslationAlg {
	
	/*
	 * It gives a specification as result
	 */
	public ISpec getSpec(Bpmn<BpmnControlFlow<FlowNode>, FlowNode> bpmn);
}
