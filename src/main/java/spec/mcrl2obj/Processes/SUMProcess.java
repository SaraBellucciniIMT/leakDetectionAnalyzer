package spec.mcrl2obj.Processes;

import java.util.Set;

import org.javatuples.Pair;

import com.google.common.collect.Sets;

import sort.ISort;
import sort.Placeholder;
import spec.mcrl2obj.Action;
import spec.mcrl2obj.MCRL2;
import spec.mcrl2obj.Operator;

public class SUMProcess extends Process {

	private String sortName;
	private Action action;
	/**
	 * Constructor for the SUMProcess class
	 * 
	 * @param sortName of the placehoders in the process
	 * @param lenght   number of placehoders to be generated
	 */
	public SUMProcess(String sortName, int lenght) {
		super(Operator.SUM);
		this.sortName = sortName;
		this.parameters = new ISort[lenght];
		for (int i = 0; i < lenght; i++)
			this.parameters[i] = new Placeholder(sortName);
		this.action = MCRL2.getInputAction(this.parameters);
	}

	/**
	 * Another constructor for the SUMProcess class
	 * 
	 * @param p the placeholder in the process
	 */
	public SUMProcess(Action a) {
		super(Operator.SUM);
		if (a.getParameters().length != 0) {
			this.parameters = a.getParameters();
			this.sortName = this.parameters[0].getNameSort();
		}
		this.action =  a;
	}

	/**
	 * Returns the action i, i.e. sum e:Sort.i(e)
	 * @return the action i, i.e. sum e:Sort.i(e)
	 */
	public Action getAction() {
		return this.action;
	}
	@Override
	public Pair<String,Set<String>> toPrint() {
		return Pair.with(toString()	,Sets.newHashSet());
	}
	
	@Override
	public String toString() {
		String s = this.parameters[0].toString();
		for (int i = 1; i < this.parameters.length; i++)
			s += "," + this.parameters[i].toString();
		return getOperator().getValue() + " " + s + ":" + sortName+ Operator.DOT.getValue()+this.action.toString();
	}
}
