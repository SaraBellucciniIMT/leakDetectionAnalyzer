/**
 * 
 */
package pcrrlalgoelement;

import pcrrlalgo.IParout;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.CommunicationFunction;

/**
 * @author sara
 *
 */
public abstract class AbstractParaout implements IParout{

	/*
	 * Update:
	 * Action set - Communication set - Allow set - Hide set
	 */
	protected void communicationFunctionUpdateSet(Parout parout,Action t1,Action t2, Action codomain) {
		parout.addCommunicationFunction(new CommunicationFunction(codomain,t1.getName(),t2.getName()));
		parout.addAct(t1,codomain);
		parout.addAllowAct(codomain);
		parout.addHideAct(codomain);
	}
	
}
