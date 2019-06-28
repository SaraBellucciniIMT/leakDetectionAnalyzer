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
import spec.mcrl2obj.Sort;
import spec.mcrl2obj.StructSort;

/**
 * @author sara
 *
 *         Abstract class that define how to compute the control flow of a bpmn
 *         model
 *
 */
public abstract class AbstractTranslationAlg implements ITranslationAlg {

	protected Tmcrl analyzeControlFlow(Bpmn<BpmnControlFlow<FlowNode>, FlowNode> bpmn) {
		return new Tmcrl(new ExploitedRPST(new RPST<ControlFlow<FlowNode>, FlowNode>(bpmn)), bpmn.getName(), bpmn.getId());
	}

	protected abstract void analyzeData();

	private Sort sortData = new StructSort("Data");
	private Sort sortMemory = new Sort("Memory");
	private Sort sortBool = new Sort("Bool");
	
	protected Sort getSortData() {
		return this.sortData;
	}
	protected Sort getSortMemory() {
		if(sortMemory.getTypes().isEmpty())
			sortMemory.addType(" Set(Data)");
		return this.sortMemory;
	}

	protected Sort getSortBool() {
		return this.sortBool;
	}
}
