package rpstTest;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import javax.sql.rowset.RowSetProvider;

import org.apache.commons.lang3.tuple.Pair;
import org.jbpt.algo.tree.rpst.RPST;
import org.jbpt.algo.tree.tctree.TCType;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.bpmn.AlternativeGateway;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.bpmn.BpmnControlFlow;
import org.jbpt.pm.bpmn.EndEvent;
import org.jbpt.pm.bpmn.StartEvent;
import org.jbpt.pm.bpmn.Task;

import algo.CollaborativeAlg;
import algo.interpreter.Tmcrl;
import io.ExploitedRPST;
import io.ExtendedNode;
import spec.mcrl2obj.MCRL2;

public class App {
	
	public static void main(String[] args) {
		
		/*Pair<Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>, Set<Pair<FlowNode, FlowNode>>> setBpmn = IOTerminal.parsebpmnfile(new File("C:/Users/Sara/eclipse-workspace/leakDetectionAnalyzer/pe-bpmn models/secretsharingtechnologies/violation/ssharing2parties(withviolation).bpmn"));
		// Create the mcrl2 object describing the collaboration
		MCRL2 mcrl2 = new CollaborativeAlg(setBpmn).getSpec();
		mcrl2.toFile(IOTerminal.dirname.getPath() + "result");*/
		try {
			IOTerminal t = new IOTerminal();
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*FlowNode start = new StartEvent();
		FlowNode a = new Task("a");
		FlowNode xora = new AlternativeGateway("xora");
		FlowNode b = new Task("b");
		FlowNode xorc = new AlternativeGateway("xorc");
		FlowNode end = new EndEvent();
	
		Bpmn<BpmnControlFlow<FlowNode>,FlowNode> bpmn = new Bpmn<>();
		bpmn.addControlFlow(start, a);
		bpmn.addControlFlow(a,xora);
		bpmn.addControlFlow(xora,b);
		bpmn.addControlFlow(b,xorc);
		bpmn.addControlFlow(xorc,xora);
		bpmn.addControlFlow(xorc,end);
		RPST<ControlFlow<FlowNode>, FlowNode> rpst = new RPST<>(bpmn);	
		ExploitedRPST exrpst = new ExploitedRPST(rpst);
		//Tmcrl t = new Tmcrl(exrpst, "party", "1");
		
		System.out.println(exrpst.toString());
	*/
	}
}
