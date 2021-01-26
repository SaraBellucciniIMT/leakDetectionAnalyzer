/**
 * 
 */
package pcrrlalgoelement;

import java.util.HashSet;
import java.util.Set;

import pcrrlalgo.IParout;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.CommunicationFunction;
import spec.mcrl2obj.MCRL2;
import spec.mcrl2obj.Operator;
import spec.mcrl2obj.Processes.AbstractProcess;
import spec.mcrl2obj.Processes.Process;

/**
 * 
 * This is the AbstractParaout class. It defines function and variable used while applying the parout function.
 * 
 * @see #communicationFunctionUpdateSet(Action, int) it updates the comm,allow and hide sets with the new function
 * @see #hasParallel(AbstractProcess) it checks if the abstract process contain a parallel process or not
 * 
 * @author S. Belluccini
 *
 */
public abstract class AbstractParaout implements IParout {

	protected static Set<CommunicationFunction> commSet = new HashSet<CommunicationFunction>();
	protected static Set<Action> allowSet = new HashSet<Action>();
	protected static Set<Action> actSet = new HashSet<Action>();
	/**
	 * Updates comm set, allow set and hide set
	 * @param domain 's action of the communication function
	 * @param size number of actions in the comm function
	 */
	protected void communicationFunctionUpdateSet(Action domain, int size) {
		Action[] d = new Action[size];
		for(int i=0; i<size; i++)
			d[i] = domain;
		Action sendread = new Action(MCRL2.getComFunResult(), domain.getParameters());
		commSet.add(new CommunicationFunction(sendread,d));
		allowSet.add(sendread);
		actSet.add(domain);
	}

	/**
	 * Checks if the given abstract process has a subprocess with the parallel operator
	 * @param p the abstract process to check
	 * @return true if it has a subprocess with the parallel operator, false otherwise
	 */
	public static boolean hasParallel(AbstractProcess p) {
		Process process;
		if (p.getClass().equals(Process.class)) {
			process = (Process) p;
			for (AbstractProcess ap : process) {
				if (ap.getClass().equals(Process.class) && ((Process) ap).getOperator().equals(Operator.PARALLEL))
					return true;
			}
		}
		return false;
	}

}
