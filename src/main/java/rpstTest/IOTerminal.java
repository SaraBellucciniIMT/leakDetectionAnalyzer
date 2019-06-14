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
import formula.PartecipantFormula;
import formula.TaskFormula;
import formula.TextInterpreterFormula;
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

				while(continueOrExit()) {
				System.out.println(
						"Verification \n -1 to check your personal formula \n -2 chec SecretSharingViolation \n -3 exit \n Choose your action: \n");
				scan = new Scanner(System.in);
				String number = scan.nextLine();
				String partecipant ;
				switch (Integer.valueOf(number)) {
				case 1:
					partecipant = scanTaskOrPartecipant();
					datset.addAll(scanData());
					if (mcrl2.toStringPartecipants().contains(partecipant))
						PartecipantFormula.toFile(mcrl2, partecipant, datset,"");
					else
						TaskFormula.toFile(mcrl2, partecipant, datset,"");

					break;
				case 2:
					partecipant = scanTaskOrPartecipant();
					TextInterpreterFormula.toFile(mcrl2, partecipant, datset, TextInterpreterFormula.violation);
				case 3:
					System.exit(0);
				default:
					break;
				}
			}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private String scanTaskOrPartecipant() {
		System.out.println("Choose partecipant/task: ");
		scan = new Scanner(System.in);
		return scan.nextLine();
	}
	
	private Set<String> scanData() {
		Set<String> datset = new HashSet<>();
		System.out.println("Choose data (, in the middle): ");
		scan = new Scanner(System.in);
		String s = scan.nextLine();
		String[] split = s.split(",");
		for (int i = 0; i < split.length; i++)
			datset.add(split[i]);

		return datset;
	}
	private boolean continueOrExit() {
		System.out.println("CONTINUE (Y/N) : ");
		String r = scan.nextLine();
		while (!r.equalsIgnoreCase(NO) && !r.equalsIgnoreCase(YES)) {
			System.out.println("command not available, try again ... ");
			r = scan.nextLine();
		}
		if(r.equals(YES))
			return true;
		if (r.equalsIgnoreCase(NO))
			System.exit(0);	
		return false;
	}
}
