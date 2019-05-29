package rpstTest;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jbpt.algo.tree.rpst.IRPSTNode;
import org.jbpt.algo.tree.rpst.RPST;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.bpmn.BpmnControlFlow;

import algo.CollaborativeAlg;
import io.BpmnParser;
import spec.mCRL2;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {

		try {
			Pair<Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>,Set<Pair<FlowNode,FlowNode>>> set = BpmnParser
					.collaborationParser("C:\\Users\\sara\\eclipse-workspace\\rpstTest\\bpmnfile\\2ucolseq.bpmn");
			
			
			
			CollaborativeAlg translationalg = new CollaborativeAlg(set);
			mCRL2 mcrl2 = translationalg.getSpec();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void traverRPST(IRPSTNode<ControlFlow<FlowNode>, FlowNode> node,
			RPST<ControlFlow<FlowNode>, FlowNode> rpst) {
		Collection<IRPSTNode<ControlFlow<FlowNode>, FlowNode>> collection = rpst.getDirectSuccessors(node);
		System.out.println("---------");
		System.out.println("Label: " + node.getLabel() + " TYPE: " + node.getType());
		if (node.getEntry() != null)
			System.out.println("Entry: " + node.getEntry() + " Entry TyPE " + node.getEntry().getTag());
		if (node.getExit() != null)
			System.out.println("Exit: " + node.getExit() + " Exit TyPE " + node.getExit().getTag());
		System.out.println("Successors: " + collection.toString());
		System.out.println("---------");
		if (!collection.isEmpty())
			for (IRPSTNode<ControlFlow<FlowNode>, FlowNode> n : collection)
				traverRPST(n, rpst);

	}
}
