package spec.mcrl2obj;

import org.apache.commons.lang3.ArrayUtils;

public class Action {

	private String name = "";
	private String secondName = "";
	private static int index = 0;
	private DataParameter[] parameters;
	/*
	 * Id that correspond to the field in the .bpmn file Example: <bpmn2:task
	 * id="Task_07fz7yg" name="A">
	 */
	private String id;
	// isParameter == true only if the Action is representing a sum, for example
	// e1...en :Data
	private boolean isParameter = false;
	private static final String tau = "tau";
	private static final String input = "i";
	private static final String output = "o";
	private static final String sendread = "sendread";
	private static final String temporary = "t";
	private static final String memory = "memory";
	private boolean istau = false;

	public Action(String name, DataParameter... dataParameters) {
		this.name = name;
		this.parameters = dataParameters;
	}

	/*
	 * Generate a silent event
	 */
	public Action() {
		istau = true;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setSecondName(String secondName) {
		this.secondName = secondName;
	}
	public String getSecondName() {
		return this.secondName;
	}
	public String getId() {
		return this.id;
	}

	// An action that has only parameters is used to represent sum: e1,...en : Data
	private Action(DataParameter... parameters) {
		this.name = "";
		this.parameters = new DataParameter[parameters.length];
		this.parameters = parameters;
		this.isParameter = true;
	}

	public static Action setMemoryAction(Sort sort) {
		return new Action(memory + (index++), new DataParameter("m", sort));
	}

	public static Action setSendReadAction(DataParameter... dataParameters) {
		return new Action(sendread, dataParameters);
	}

	public static Action setTemporaryAction() {
		return new Action(temporary + (index++));
	}

	public static Action setTemporaryAction(DataParameter... parameters) {
		return new Action(temporary + (index++), parameters);
	}

	public String getName() {
		return this.name;
	}

	public String getSortString() {
		String s = "";
		if (this.id != null)
			return "Memory";
		else {
			for (int i = 0; i < this.parameters.length; i++) {
				s = s + this.parameters[i].getSort().getName();
				if (i != this.parameters.length - 1)
					s = s + "#";
			}
			return s;
		}
	}

	public DataParameter[] getParameters() {
		return parameters;
	}

	public int nparameter() {
		return parameters.length;
	}

	// Standard representation for a INPUT CHANNEL with n parameters
	public static Action inputAction(DataParameter... parameters) {
		return new Action(input + (index++), parameters);
	}

	// Standard representation for an OUTPUT CHANNEL with n parameters
	public static Action outputAction(DataParameter... parameters) {
		return new Action(output + (index++), parameters);
	}

	public static Action sumAction(DataParameter... parameter) {
		return new Action(parameter);
	}

	/*
	 * Add 1 parameter to this action
	 */
	public void addDataParameter(DataParameter parameter) {
		if (!istau)
			this.parameters = ArrayUtils.add(this.parameters, parameter);
		else
			System.err.println("You cannot add parameters to a TAU action");
	}

	public boolean isParametrized() {
		if (this.parameters.length == 0)
			return false;
		else
			return true;
	}

	@Override
	public String toString() {
		if (istau)
			return tau;
		else if (id != null) {
			String s = name;
			if (parameters != null && parameters.length != 0)
				return s + "(union({},{" + organizeParameterAsString() + "}))";
			else
				return s + "({})";
		} else if (isParameter) {
			return organizeParameterAsString() + ": " + parameters[0].getSort().getName();
		} else if (parameters.length != 0 && !this.name.isEmpty())
			return name + "(" + organizeParameterAsString() + ")";
		else
			return name;
	}

	public boolean isTau() {
		return istau;
	}

	private String organizeParameterAsString() {
		String s = "";
		for (int i = 0; i < parameters.length; i++) {
			s = s + parameters[i];
			if (i != parameters.length - 1)
				s = s + ",";
		}
		return s;
	}

	public boolean containsParameter(DataParameter d) {
		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i].equals(d))
				return true;
		}
		return false;
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
		} else if (nparameter() != other.nparameter())
			return false;
		return true;
	}
}
