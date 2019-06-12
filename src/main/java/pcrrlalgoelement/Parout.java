package pcrrlalgoelement;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import spec.mcrl2obj.AbstractProcess;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.CommunicationFunction;
import spec.mcrl2obj.Operator;
import spec.mcrl2obj.Process;
import spec.mcrl2obj.mCRL2;

public class Parout {

	private mCRL2 mcrl2;

	public mCRL2 parout(mCRL2 mcrl2) {
		this.mcrl2 = mcrl2;
		callParout();
		System.out.println("OLD PROCESSES" + mcrl2.getProcesses().toString());
		return mcrl2;
	}

	private void callParout() {
		System.out.println(mcrl2.getInitSet());
		boolean parout = true;
		while (parout) {
			parout = false;
			for (AbstractProcess ap : mcrl2.getProcesses()) {
				System.out.println(ap.toString());
				Pair<Process, Process> pair;

				if (ap.getClass().equals(Process.class) && ((pair = hasParallel((Process) ap)) != null)) {
					parallel(pair.getLeft(), pair.getRight());
					parout = true;
					break;
				}
			}
		}
	}

	private Pair<Process, Process> hasParallel(Process p) {
		// This means that the process is an action because otherwise cannot have just
		// one child
		if (p.getAction() != null)
			return null;
		for (int i = 0; i < p.getLength(); i++) {
			if(p.inInsideDef(p.getChildName(i))!= null)
				continue;
			AbstractProcess child = mcrl2.identifyAbstractProcess(p.getChildName(i));
			System.out.println("CHILD : " + child.toString());
			if (child.getClass().equals(Process.class) && ((Process) child).getAction() == null
					&& ((Process) child).getOperator().equals(Operator.PARALLEL))
				return Pair.of(p, (Process) child);
		}
		return null;
	}

	private void parallel(Process dad, Process child) {
		if (dad.getOperator().equals(Operator.DOT) && dad.getLength() >1)
			new Sequence().interpreter(this, dad, child);
		else if (dad.getOperator().equals(Operator.PLUS) && dad.getLength() >1)
			new CHOICE().interpreter(this, dad, child);
		else {
			dad.setOpertor(child.getOperator());
			List<String> newchilds = new ArrayList<String>();
			for(int i=0; i<child.getLength(); i++)
				newchilds.add(child.getChildName(i));
			dad.setChild(newchilds);
			removeProcess(child.getName());
		}
			

	}

	protected void removeProcess(String name) {
		this.mcrl2.removeProcess(name);
	}

	protected void addCommunicationFunction(CommunicationFunction f) {
		this.mcrl2.addCommunicaitonFunction(f);
	}

	protected void addHideAct(Action a) {
		this.mcrl2.addHide(a);
	}

	protected void addAllowAct(Action a) {
		this.mcrl2.addAllow(a);
	}

	protected void addAct(Action a) {
		this.mcrl2.addAction(a);
	}

	protected void addProcess(Process p) {
		this.mcrl2.addProcess(p);
	}

}
