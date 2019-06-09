/**
 * 
 */
package spec.mcrl2obj;


/**
 * @author sara
 *
 */
public class Buffer extends TaskProcess {
	
	private static final Operator op = Operator.PLUS;
	private TaskProcess operand1 ;
	private Process operand2;

	public Buffer(TaskProcess op1 , Process op2) {
		super();
		operand1 = op1;
		operand2 = op2;
	}
	
	@Override
	public String toString() {
		return operand1.toString() + op.getValue() + operand2.toString();
	}
	

}
