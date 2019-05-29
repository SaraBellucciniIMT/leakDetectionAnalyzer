package algo.interpreter;

import spec.mcrl2obj.Process;

public interface ITProcess {
	
	Process interpreter(Tmcrl node);
}
