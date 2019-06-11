package rpstTest;

import java.io.IOException;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.bpmn.BpmnControlFlow;

import algo.CollaborativeAlg;
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
	private static Scanner scan;
	private final static String YES = "Y";
	private final static String NO = "N";
	public static void main(String[] args) {
		while(true) {
			
			System.out.println("Inser filename : ");
			scan = new Scanner(System.in);
			String filename = scan.nextLine();
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
			System.out.println("CONTINUE (Y/N) : ");
			String r = scan.nextLine();
			while(!r.equalsIgnoreCase(NO) && r.equalsIgnoreCase(YES)) {
				System.out.println("command not available, try again ... ");
				r = scan.nextLine();
			}	
			if(r.equalsIgnoreCase(NO))
				System.exit(0);
		}
	}

}
