package algo.interpreter;

import spec.mcrl2obj.AbstractProcess;


public interface ITProcess {
	
	AbstractProcess interpreter(Tmcrl node);
	
}
