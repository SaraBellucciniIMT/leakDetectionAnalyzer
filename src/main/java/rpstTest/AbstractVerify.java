package rpstTest;

import spec.mcrl2obj.mCRL2;

public abstract class AbstractVerify {

	protected mCRL2 mcrl2;
	
	abstract protected void addSort();
	
	abstract protected void addMaps();
	
	abstract protected void addEqn();
	
	abstract protected void computeProcess();
	
	abstract protected void addViolation();
	
	public mCRL2 getMCRL2() {
		
		addSort();
		addMaps();
		addEqn();
		computeProcess();
		addViolation();
		
		return mcrl2;
	}

}
