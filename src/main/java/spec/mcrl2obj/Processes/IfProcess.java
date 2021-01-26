package spec.mcrl2obj.Processes;

import java.util.Set;

import org.javatuples.Pair;

import com.google.common.collect.Sets;

/**
 * This is the IfProcess class representing conditions in the mcrl2 specification language
 * @author S. Belluccini
 *
 */
public class IfProcess extends AbstractProcess {
	
	private String condition;
	private AbstractProcess trueSide;
	private AbstractProcess falseSide;
	
	/**
	 * Constructor for If Process with condition, true statement, false statement. Generate a process with this structure () -> <>
	 * @param condition string describing the condition
	 * @param tSide true statement
	 * @param fSide false statement
	 */
	public IfProcess(String condition, AbstractProcess tSide, AbstractProcess fSide) {
		this.condition = condition;
		this.trueSide = tSide;
		this.falseSide = fSide;
	}
	
	/**
	 * Another constructor for the IfProcess class with condition and true statement. Generates a process with this structure () -> 
	 * @param condition string describing the condition
	 * @param tSide true statement
	 */
	public IfProcess(String condition,AbstractProcess tSide) {
		this.condition = condition;
		this.trueSide = tSide;
	}
	
	/**
	 * Prints a string with this structure ( condition ) -> if true <> if false
	 */
	@Override
	public Pair<String,Set<String>> toPrint() {
		return Pair.with(toString()	,Sets.newHashSet());
	}
	
	public String toString() {
		String s = "( " + condition + " ) ->";
		s += trueSide.toString();
		if(falseSide!= null) {
			s += "<>" + falseSide.toString();
		}
		return s;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}
	
	
}
