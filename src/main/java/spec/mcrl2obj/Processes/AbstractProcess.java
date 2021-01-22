package spec.mcrl2obj.Processes;

import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.javatuples.Pair;

import rpstTest.Utils;
import sort.ISort;

/**
 * Abstract class for process definition that defines an id for every object
 * that implements this class, if it is not already set
 * 
 * @author S. Belluccini
 */
public abstract class AbstractProcess {

	protected ISort[] parameters = new ISort[0];;
	protected String id;
	private static final String fiexdName = "P";
	
	public String getId() {
		if (id == null)
			id = fiexdName + Utils.getId();
		return id;
	}

	public abstract Pair<String,Set<String>> toPrint();

	/**
	 * Parameters p1...pn of the process, i.e. P(p1...pn) = ...
	 * @param parameters in input to the process
	 */
	public void addParameters(ISort... parameters) {
		if (this.parameters.length == 0)
			this.parameters = parameters;
		else {
			ArrayUtils.addAll(this.parameters, parameters);
		}
	}

	/**
	 * Returns the parameters taken in input from this process. Given a process P(a,b,c) it returns [a,b,c]
	 * @return the parameters taken in input from this process. 
	 */
	public ISort[] getParameters() {
		return parameters;
	}

	/**
	 * Returns the parameters p1,...pn printed as : p1:Sort,...,pn:Sort
	 * @return
	 */
	public String printParameters() {
		String param="";
		for(ISort s : parameters) {
			if(!param.isEmpty())
				param += ",";
			param += s.toString() + ":" + s.getNameSort(); 
		}
		return param;
	}
	/**
	 * Returns the number of parameters taken as input from this process. Given a process
	 * P(a,b,c) the result will be 3
	 * 
	 * @return the number of parameters taken as input from this process.
	 */
	public int parametersLenght() {
		return parameters.length;
	}
}
