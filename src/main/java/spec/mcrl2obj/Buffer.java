/**
 * 
 */
package spec.mcrl2obj;

import rpstTest.Utils;

/**
 * @author sara
 *
 */
public class Buffer extends TaskProcess {

	private static final Operator op = Operator.PLUS;
	private TaskProcess operand1;
	private Process operand2;
	private DataParameter[] initialParameters;
	private DataParameter[] givenParameters;

	public Buffer(DataParameter[] givenparameter, Sort sort) {
		super();
		this.initialParameters = new DataParameter[givenparameter.length];
		// Buffer(d_1,...,d_n)
		for (int i = 0; i < givenparameter.length; i++)
			initialParameters[i] = new DataParameter(sort);
		this.givenParameters = givenparameter;
	}

	public void setOperant(TaskProcess op1, Process op2) {
		operand1 = op1;
		operand2 = op2;
	}

	@Override
	public String toString() {
		String s = getName().toString() + "(" + Utils.organizeParameterAsString(initialParameters) + ":"
				+ initialParameters[0].getSort().getName() + ") = ";
		s = s + "(" + operand1.toStringinputAction() + getName().toString() + "("
				+ Utils.organizeParameterAsString(givenParameters) + ")" + ")";
		s = s + op.getValue() + "(" + operand2.toString();
		s = s + "." + getName().toString() + "(" + Utils.organizeParameterAsString(initialParameters) + ")" + ")";

		return s;
	}

	public DataParameter[] getInitialParameters() {
		return this.initialParameters;
	}

}
