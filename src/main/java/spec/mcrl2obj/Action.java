package spec.mcrl2obj;

import java.util.HashSet;
import java.util.Set;

public class Action{

	private String name;
	private static int id =0;
	private Set<DataParameter> parameters;
	private boolean isParameter = false;
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
	public static Action setSendAction(Set<DataParameter> dataParameters) {
		return new Action(send,dataParameters);
	}
	public static Action setSendAction() {
		return new Action(send);
	}
	
	public static Action setReadAction() {
		return new Action(read);
		
	}
	public static Action setReadAction(Set<DataParameter> dataParameters) {
		return new Action(read, dataParameters);
		
	}
	//An action that has only parameters is used to represent sum: e1,...en : Data 
	private Action(Set<DataParameter> parameters) {
		this.name="";
		this.parameters = parameters;
		this.isParameter = true;
	}
	
	public String getName() {
		return this.name;
	}
	public Set<DataParameter> getParameters(){
		return parameters;
	}
	
	public int nparameter() {
		return parameters.size();
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
	public static Action emptyParameterAction(String name) {
		Set<DataParameter> parameters = new HashSet<DataParameter>();
		parameters.add(DataParameter.setEmptyParameter());
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
		else if(isParameter) {
			return  organizeParameterAsString() + ": Data";
		}else if (!parameters.isEmpty() && !this.name.isEmpty())
			return name + "(" + organizeParameterAsString() + ")";
		else
			return name;
	}
	
	public boolean isTau() {
		return istau;
	}

	private String organizeParameterAsString() {
		String s= "";
		int i =0;
		for(DataParameter d : parameters) {
			s = s+d;
			if(i != parameters.size()-1)
				s = s + ",";
			i++;
		}
		return s;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Action other = (Action) obj;
		if (isParameter != other.isParameter)
			return false;
		if (istau != other.istau)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (nparameter() !=other.nparameter())
			return false;
		return true;
	}
}
