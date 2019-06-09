package pcrrlalgoelement;

import pcrrlalgo.IParout;
import spec.mcrl2obj.Process;

/*
 * The basic element in this case is an aciton element then
 * parout(a) = a;
 */
public class BasicElement implements IParout {

	@Override
	public Process interpreter(Process p) {
		// TODO Auto-generated method stub
		
		return p;
	}

	
}
