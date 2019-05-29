package algo.interpreter;

import spec.mcrl2obj.Action;
import spec.mcrl2obj.Process;

public class EndEvent implements ITProcess{

	@Override
	public Process interpreter(Tmcrl node) {
		// TODO Auto-generated method stub
			return new Process(new Action());
		
	}

}
