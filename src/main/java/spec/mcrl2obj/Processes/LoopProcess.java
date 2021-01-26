package spec.mcrl2obj.Processes;

import java.util.HashSet;
import java.util.Set;

import org.javatuples.Pair;

import com.google.common.collect.Sets;

import spec.mcrl2obj.Action;
import spec.mcrl2obj.Operator;

/**
 * This is the loop class , it represents loop process in the specification. A
 * loop is a process s.t. : L = body.L + body;
 * 
 * @author S. Belluccini
 *
 */
public class LoopProcess extends Process {

	/**
	 * Constructor for the LoopProcess class. It defines a process s.t.
	 * L = body.L + body;
	 * @param process the body of the loop
	 */
	public LoopProcess(AbstractProcess process) {
		super(Operator.PLUS, process);
		//Process repeat = new Process(Operator.DOT, process, new Action(this.getId(), this.getId()));
		//this.addChildAtPosition(0, repeat);

	}

	/**
	 * {@inheritDoc} It defines how a loop process is printed in a mcrl2
	 * specification. It returns a pair where on the left, there is the process
	 * definition, i.e. its name, while on the right the process itself.
	 */
	@Override
	public Pair<String, Set<String>> toPrint() {
		Pair<String,Set<String>> left = this.getChildAtPosition(0).toPrint();
		Set<String> definitions = new HashSet<>(left.getValue1());
		definitions.add(this.toString());
		return Pair.with(this.getId(),definitions);
	}

	public String toString() {
		Process repeat = new Process(Operator.DOT, this.getChildAtPosition(0), new Action(this.getId(), this.getId()));
		String me = this.getId() +"=" +repeat + this.getOperator().getValue() +this.getChildAtPosition(0).toString();
		return me;
	}
}
