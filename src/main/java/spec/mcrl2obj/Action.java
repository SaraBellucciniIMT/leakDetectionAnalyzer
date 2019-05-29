package spec.mcrl2obj;

import java.util.HashSet;
import java.util.Set;

public class Action {

	private String name;
	private static int id =0;
	private Set<DataParameter> parameters;
	private static final String tau = "tau";
	private static final String input = "i";
	private static final String output = "o";
	private static final String send = "send";
	private static final String read = "read";
	private boolean istau = false;

	/*
	 * Action with an empty set of parameters
	 */
	public Action(String name) {
		this.name = name;
		this.parameters = new HashSet<DataParameter>();
	}

	/*
	 * Parametrized action
	 */
	public Action(String name, Set<DataParameter> dataParameters) {
		this.name = name;
		this.parameters = dataParameters;
	}
	/*
	 * Generate a silent event
	 */
	public Action() {
		istau = true;
	}
	
	//An action that has only parameters is used to represent sum: e1,...en : Data 
	private Action(Set<DataParameter> parameters) {
		this.parameters = parameters;
	}
	
	public String getName() {
		return this.name;
	}
	
	//Standard representation for a INPUT CHANNEL with n parameters
	public static Action inputAction(Set<DataParameter> parameters) {
		return new Action(input + (id++),parameters);
	}
	
	
	//Standard representation for an OUTPUT CHANNEL with n parameters
	public static Action outputAction(Set<DataParameter> parameters) {
		return new Action(output + (id++),parameters);
	}
	
	// Gives as result an action of this form : sum: e1,...en : Data 
	public static Action sumAction(Set<DataParameter> parameters) {
		return new Action(parameters);
	}
	
	// Generate an action as following : o(eps), a(eps) etc..
	public static Action emptyParameterAction(String name,Sort sort) {
		Set<DataParameter> parameters = new HashSet<DataParameter>();
		parameters.add(DataParameter.setEmptyParameter(sort));
		return new Action(name, parameters);
	}
	/*
	 * Add 1 parameter to this action
	 */
	public void addDataParameter(DataParameter parameter) {
		if(!istau)
			this.parameters.add(parameter);
		else
			System.err.println("You cannot add parameters to a TAU action");
	}

	public boolean isEmpty() {
		if (this.name.isEmpty())
			return true;
		else
			return false;
	}

	public boolean isParametrized() {
		if (this.parameters.isEmpty())
			return false;
		else
			return true;
	}

	@Override
	public String toString() {
		if (istau)
			return tau;
		else if (!parameters.isEmpty())
			return name + "(" + parameters + ")";
		else
			return name;
	}

}
