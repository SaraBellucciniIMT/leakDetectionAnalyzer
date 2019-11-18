package pcrrlalgoelement;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import spec.mcrl2obj.AbstractProcess;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.CommunicationFunction;
import spec.mcrl2obj.Operator;
import spec.mcrl2obj.PartecipantProcess;
import spec.mcrl2obj.Process;
import spec.mcrl2obj.mCRL2;

public class Parout {

	private mCRL2 mcrl2;

	public mCRL2 parout(mCRL2 mcrl2) {
		this.mcrl2 = mcrl2;
		callParout();
		return mcrl2;
	}

	private void callParout() {
		boolean parout = true;
		while (parout) {
			parout = false;
			for (AbstractProcess ap : mcrl2.getProcesses()) {
				Pair<Process, Process> pair;
				if (ap.getClass().equals(Process.class) && ((pair = hasParallel (ap)) != null)
						|| (ap.getClass().equals(PartecipantProcess.class)
								&& (pair = hasParallel(ap)) != null)) {
					parout = parallel(pair.getLeft(), pair.getRight());
					break;
				}
			}
		}
	}

	private Pair<Process, Process> hasParallel(AbstractProcess process) {
		// This means that the process is an action because otherwise cannot have just
		// one child
		Process p ;
		if(process.getClass().equals(Process.class))
			p = (Process)process;
		else
			p=((PartecipantProcess) process).getProcess();
		if (p.getAction() != null)
			return null;
		for (int i = 0; i < p.getLength(); i++) {
			if (p.getInsideDef(p.getChildName(i)) != null)
				continue;
			AbstractProcess child = mcrl2.identifyAbstractProcess(p.getChildName(i));

			if (child.getClass().equals(Process.class) && ((Process) child).getAction() == null
					&& ((Process) child).getOperator() != null
					&& ((Process) child).getOperator().equals(Operator.PARALLEL))
				return Pair.of(p, (Process) child);
		}
		return null;
	}

	private boolean parallel(Process dad, Process child) {
		//p1.(p2||...||.p3)
		if (dad.getOperator().equals(Operator.DOT) && dad.getLength() > 1)
			new Sequence().interpreter(this, dad, child);
		//p1+(p2||...||p3)
		else if (dad.getOperator().equals(Operator.PLUS) && dad.getLength() > 1)
			new CHOICE().interpreter(this, dad, child);
		//p1||(p2||...||p3)
		else if (dad.getOperator().equals(Operator.PARALLEL))
			new Parallel().interpreter(this, dad, child);
		else {
			dad.setOpertor(child.getOperator());
			List<String> newchilds = new ArrayList<String>();
			for (int i = 0; i < child.getLength(); i++)
				newchilds.add(child.getChildName(i));
			dad.setChild(newchilds);
			removeProcess(child.getName());
		}
		return true;

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

	protected void addAct(Action... a) {
		this.mcrl2.addAction(a);
	}

	protected void addProcess(Process p) {
		this.mcrl2.addProcess(p);
	}

}
