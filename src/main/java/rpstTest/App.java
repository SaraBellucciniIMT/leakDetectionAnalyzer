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
import spec.mcrl2obj.mCRL2;

/**
 * TO-DO: 
 * VENERDì : runnare secondo test e fixing di possibili problemi
 * SABATO : IMPLEMENTARE MIGLIORAMENTO RIMOZIONE TAU;
 * DOMENICA : RISOLVI PARALELLISMI (VEDIZIONE FUNZIONE PAROUT DAL PAPER);
 * LUNEDì : INTRODUCI RICORSIONE
 * MARTEDì : FORMULE
 * MERCOLEDì : INTERFACCIA ESTERNA
 */
public class App {
	public static void main(String[] args) {

		try {
			Pair<Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>,Set<Pair<FlowNode,FlowNode>>> set = BpmnParser
					.collaborationParser("C:\\Users\\sara\\eclipse-workspace\\rpstTest\\bpmnfile\\colseq.bpmn");
			
			
			
			CollaborativeAlg translationalg = new CollaborativeAlg(set);
			mCRL2 mcrl2 = translationalg.getSpec();
			System.out.println(mcrl2.toString());
			mcrl2.toFile("prova");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
