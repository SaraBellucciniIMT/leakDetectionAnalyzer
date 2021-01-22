package pcrrlalgoelement;

import java.util.Set;
import org.javatuples.Quartet;

import spec.mcrl2obj.Action;
import spec.mcrl2obj.CommunicationFunction;
import spec.mcrl2obj.Operator;
import spec.mcrl2obj.Processes.AbstractProcess;
import spec.mcrl2obj.Processes.ParticipantProcess;
import spec.mcrl2obj.Processes.Process;

public class Parout {

	/**
	 * Returns a quartet with the results of the Tp appliation over an abstractprocess.
	 * @param process the abstrac process
	 * @return a quartet with the new abstract process , communication function set, allow action set, action set
	 */
	public static Quartet<ParticipantProcess, Set<CommunicationFunction>, Set<Action>, Set<Action>>  parout(ParticipantProcess process) {
		AbstractProcess p = Tp(process.getProcess());
		process.setProcess(p);
		return Quartet.with(process, AbstractParaout.commSet, AbstractParaout.allowSet, AbstractParaout.actSet);
	}

	protected static AbstractProcess Tp(AbstractProcess ap) {
		if (!ap.getClass().equals(Process.class))
			return ap;
		else if (((Process)ap).getOperator() == Operator.PARALLEL)
			ap = new Parallel().interpreter((Process)ap);
		else if (((Process)ap).getOperator() == Operator.DOT) {
			ap = new Sequence().interpreter((Process)ap);
		} else if (((Process)ap).getOperator() == Operator.PLUS) {
			ap = new Choice().interpreter((Process)ap);
		}
		return ap;
	}

}
