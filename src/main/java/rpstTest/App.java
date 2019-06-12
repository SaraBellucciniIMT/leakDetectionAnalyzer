package rpstTest;

import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.bpmn.BpmnControlFlow;

import algo.CollaborativeAlg;
import formula.TaskFormula;
import io.BpmnParser;
import pcrrlalgoelement.Parout;
import spec.mcrl2obj.mCRL2;

/**
 * TO-DO: 
 * MARTEDì : FORMULE
 * MERCOLEDì : RICORSIONE // **LIMITATION INSIDE PARALLEL**
 * GIOVEDì: Check single function
 */
public class App {
	public static void main(String[] args) {
			String filename = "colseq";
			Pair<Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>, Set<Pair<FlowNode, FlowNode>>> set = null;
			try {
				set = BpmnParser.collaborationParser("C:\\Users\\sara\\eclipse-workspace\\rpstTest\\bpmnfile\\"+filename + ".bpmn");

				CollaborativeAlg translationalg = new CollaborativeAlg(set);
				mCRL2 mcrl2 = translationalg.getSpec();
				mcrl2.taureduction();
				Parout parout = new Parout();
				mcrl2 = parout.parout(mcrl2);
				mcrl2.toFile(filename);
				System.out.println(mcrl2.toStringPartecipants());
				System.out.println(mcrl2.toStringAllTask());
				Set<String> datset = new HashSet<>();
				datset.add("data1");
				TaskFormula.toFile(mcrl2, "Q",datset);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			
	}

}
