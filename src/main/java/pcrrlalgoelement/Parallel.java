package pcrrlalgoelement;

import spec.mcrl2obj.Operator;
import spec.mcrl2obj.Processes.AbstractProcess;
import spec.mcrl2obj.Processes.Process;

public class Parallel extends AbstractParaout {

	@Override
	public AbstractProcess interpreter(Process process) {

		Process p = new Process(Operator.PARALLEL);

		for (int i = 0; i < process.getSize(); i++)
			p.addChildAtPosition(i,Parout.Tp(process.getChildAtPosition(i)));

		return p;
	}

}
