/**
 * 
 */
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
 * @author sara
 *
 */
public class IOTerminal {
	private static Scanner scan;
	private final static String YES = "Y";
	private final static String NO = "N";

	public IOTerminal() {
		while (true) {

			System.out.println("Inser filename : ");
			scan = new Scanner(System.in);
			String filename = scan.nextLine();
			Pair<Set<Bpmn<BpmnControlFlow<FlowNode>, FlowNode>>, Set<Pair<FlowNode, FlowNode>>> set = null;
			try {
				set = BpmnParser.collaborationParser(
						"C:\\Users\\sara\\eclipse-workspace\\rpstTest\\bpmnfile\\" + filename + ".bpmn");

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
				datset.add("data2");
				TaskFormula.toFile(mcrl2, "A", datset);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("CONTINUE (Y/N) : ");
			String r = scan.nextLine();
			while (!r.equalsIgnoreCase(NO) && r.equalsIgnoreCase(YES)) {
				System.out.println("command not available, try again ... ");
				r = scan.nextLine();
			}
			if (r.equalsIgnoreCase(NO))
				System.exit(0);
		}

	}
}
