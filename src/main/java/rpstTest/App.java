package rpstTest;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.bpmn.BpmnControlFlow;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;

import algo.CollaborativeAlg;
import io.BpmnParser;
import pcrrlalgoelement.Parout;
import spec.mcrl2obj.mCRL2;

/**
 * TO-DO: 
 * 
 * DOMENICA : RISOLVI PARALELLISMI (VEDIZIONE FUNZIONE PAROUT DAL PAPER); (
 * LUNEDì : INTRODUCI RICORSIONE
 * MARTEDì : FORMULE
 * MERCOLEDì : INTERFACCIA ESTERNA
 */
public class App {
	public static void main(String[] args) {
		String filename = "2ucolseq";
		Pair<Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>, Set<Pair<FlowNode, FlowNode>>> set = null;
		try {
			set = BpmnParser.collaborationParser("C:\\Users\\sara\\eclipse-workspace\\rpstTest\\bpmnfile\\"+filename + ".bpmn");

			CollaborativeAlg translationalg = new CollaborativeAlg(set);
			mCRL2 mcrl2 = translationalg.getSpec();
			mcrl2.taureduction();
			Parout parout = new Parout();
			mcrl2 = parout.parout(mcrl2);
			mcrl2.toFile(filename);
			System.out.println("TEST COLSEQ DONE");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

}
