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
		return new Tmcrl(new ExploitedRPST(new RPST<ControlFlow<FlowNode>, FlowNode>(bpmn)), bpmn.getName(),
				bpmn.getId());
	}

	protected abstract void analyzeData();

	public static int id_op;
	private static Sort sortData = new StructSort("Data");
	private static Sort sortMemory = new Sort("Memory");
	private static Sort sortBool = new Sort("Bool");
	private static StructSort sortEvalData = new StructSort("EvalData");
	public static final String empty = "eps";

	// Data = struct data1 |... | datan;
	public static Sort getSortData() {
		return sortData;
	}

	// Memory = List(EvalData);
	public static Sort getSortMemory() {
		if (sortMemory.isEmpty()) {
			if (id_op == IDOperaion.TASK.getVal() || id_op == IDOperaion.PARTICIPANT.getVal())
				sortMemory.addType(" Set(EvalData)");
			else if (id_op == IDOperaion.SSSHARING.getVal())
				sortMemory.addType(" List(EvalData)");
		}
		return sortMemory;
	}

	// Predefined mcrl2 sort
	public static Sort getSortBool() {
		return sortBool;
	}

	// EvalData = struct triple(fst:Data,snd:Bool,trd:Nat)|eps;
	public static StructSort getSortEvalData() {
		if (sortEvalData.isEmpty())
			sortEvalData.addType("triple(fst:Data,snd:Bool,trd:Nat)", empty);
		return sortEvalData;
	}
}
