/**
 * 
 */
package pcrrlalgoelement;

import java.util.ArrayList;
import java.util.List;

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
		List<Action> commset = new ArrayList<Action>();
		commset.add(t1);
		commset.add(t2);
		parout.addCommunicationFunction(new CommunicationFunction(commset, codomain));
		parout.addAct(t1);
		parout.addAct(codomain);
		parout.addAllowAct(codomain);
		parout.addHideAct(codomain);
	}
	
	protected List<String> t1Processt2(String t1,String process,String t2){
		List<String> list = new ArrayList<String>();
		list.add(t1);
		list.add(process);
		list.add(t2);
		return list;
	}
}
