package rpstTest;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.jbpt.algo.tree.rpst.IRPSTNode;
import org.jbpt.algo.tree.rpst.RPST;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.hypergraph.abs.Vertex;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.bpmn.BpmnControlFlow;

import algo.TranslationAlg;
import io.BpmnParser;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {

		try {
			Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>> set = BpmnParser
					.collaborationParser("C:\\Users\\sara\\eclipse-workspace\\rpstTest\\bpmnfile\\2ucolseq.bpmn");
			set.forEach(b -> {
				System.out.println("MODEL" + b.getId());
				b.getDataNodes().forEach(n -> {
					System.out.println(n.getId() + " R: " + n.getReadingFlowNodes().toString() + " W: " + n.getWritingFlowNodes());
				});
			});
			
			
			for(Bpmn<BpmnControlFlow<FlowNode>, FlowNode> b : set) {
				TranslationAlg f = new TranslationAlg();
				f.getSpec(b);
			}
		
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

	// Each node is identified as => LABEL : (Entry,Exit) - [Fragment]
	public static void traversRPST(IRPSTNode<DirectedEdge, Vertex> node, RPST<DirectedEdge, Vertex> rpst) {
		Collection<IRPSTNode<DirectedEdge, Vertex>> collection = rpst.getDirectSuccessors(node);
		System.out.println("Label: " + node.getLabel() + " TYPE: " + node.getType());
		System.out.println("Entry: " + node.getEntry());
		System.out.println("Exit: " + node.getExit());
		System.out.println("Successors: " + collection.toString());
		if (!collection.isEmpty()) {
			for (IRPSTNode<DirectedEdge, Vertex> n : collection)
				traversRPST(n, rpst);
		}

	}
}
