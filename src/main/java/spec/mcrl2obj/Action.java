package spec.mcrl2obj;

import org.apache.commons.lang3.ArrayUtils;

import algo.AbstractTranslationAlg;
import spec.mcrl2obj.DataParameter;
import algo.IDOperaion;
import rpstTest.Utils;

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
	private boolean isTemporary = false;
	private static final String memory = "memory";
	private boolean istau = false;
	private String pet = "";

	public Action(String name) {
		this.name = name;
		this.parameters = new DataParameter[0];
	}

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

	private String petid;

	public void setPet(String pet) {
		String[] split = pet.split("-");
		this.pet = split[0];
		if (split.length > 1)
			this.petid = split[1];
	}

	public String getPETid() {
		return this.petid;
	}

	public boolean equalPet(String petname, String id_shares) {
		if (this.pet.equals(petname) && petid.equals(id_shares))
			return true;

		return false;
	}

	// PET is given by petname-id_protection, example SSHARING-(no threshold but id
	// of the privacy label)
	public String getPet() {
		return this.pet;
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

	public void setTemporaty() {
		this.isTemporary = true;
	}

	// An action that has only parameters is used to represent sum: e1,...en : Data
	private Action(DataParameter... parameters) {
		this.name = "";
		this.parameters = new DataParameter[parameters.length];
		this.parameters = parameters;
		this.isParameter = true;
	}

	public static Action setMemoryAction(Sort sort) {
		DataParameter par = new DataParameter(sort);
		return new Action(memory + (index++), par);
	}

	public static Action setVIOLATOINAction() {
		return new Action(mCRL2.violation);
	}

	public static Action setRECOSTRUCTIONAction() {
		return new Action(mCRL2.recostruct);
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
		if (!istau) {
			this.parameters = ArrayUtils.add(this.parameters, parameter);
		} else
			System.err.println("You cannot add parameters to a TAU action");
	}

	@Override
	public String toString() {
		String rp = setRightParenthesis();
		String lp = setLeftParenthesis();
		if (istau)
			return tau;
		else if (id != null) {
			String s = name;
			if (parameters != null && parameters.length != 0)
				return s + "(union(" + lp + rp + "," + lp + Utils.organizeParameterAsString(parameters) + rp + "))";
			else
				return s + "(" + lp + rp + ")";
		} else if (isParameter) {
			return Utils.organizeParameterAsString(parameters) + ": " + parameters[0].getSort().getName();
		} else if (isTemporary) {
			String s = name;
			s = s + "(" + parameters[0].toString() + "," + lp;
			for (int i = 1; i < parameters.length; i++) {
				s = s + parameters[i];
				if (i != parameters.length - 1)
					s = s + ",";
			}
			return s + rp + ")";
		} else if (parameters.length != 0 && !this.name.isEmpty() && this.name.equals("!empty")) {
			return name + "(" + Utils.organizeParameterAsString(parameters) + ")";
		} else if (parameters.length != 0 && !this.name.isEmpty())
			return name + "(" + Utils.organizeParameterAsString(parameters) + ")";
		else
			return name;
	}

	public static String setRightParenthesis() {
		if (AbstractTranslationAlg.id_op == IDOperaion.TASK.getVal()
				|| AbstractTranslationAlg.id_op == IDOperaion.PARTICIPANT.getVal())
			return "}";
		else if (AbstractTranslationAlg.id_op == IDOperaion.SSSHARING.getVal()
				|| AbstractTranslationAlg.id_op == IDOperaion.ENCRYPTION.getVal()
				|| AbstractTranslationAlg.id_op == IDOperaion.RECONSTRUCTION.getVal())
			return "]";
		return null;
	}

	public static String setLeftParenthesis() {
		if (AbstractTranslationAlg.id_op == IDOperaion.TASK.getVal()
				|| AbstractTranslationAlg.id_op == IDOperaion.PARTICIPANT.getVal())
			return "{";
		else if (AbstractTranslationAlg.id_op == IDOperaion.SSSHARING.getVal()
				|| AbstractTranslationAlg.id_op == IDOperaion.ENCRYPTION.getVal()
				|| AbstractTranslationAlg.id_op == IDOperaion.RECONSTRUCTION.getVal())
			return "[";
		return null;
	}

	public boolean isTau() {
		return istau;
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
