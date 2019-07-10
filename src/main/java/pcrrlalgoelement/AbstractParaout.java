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
	protected void communicationFunctionUpdateSet(Parout parout,Action codomain,Action domain, int size) {
		String[] namedomain = new String[size]; 
		for(int i=0; i<size; i++)
			namedomain[i] = domain.getName();
		parout.addCommunicationFunction(new CommunicationFunction(codomain,namedomain));
		parout.addAct(domain,codomain);
		parout.addAllowAct(codomain);
		parout.addHideAct(codomain);
	}
	
}
