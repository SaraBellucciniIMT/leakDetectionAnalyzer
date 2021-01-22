package spec.mcrl2obj;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This is the CommunicationFunction class. It represents the communcation
 * function in the mcrl2 language.
 * 
 * a1|...|a_n-> r
 * 
 * @author S. Belluccini
 *
 */
public class CommunicationFunction {

	List<Action> domain = new ArrayList<Action>();
	Action codomain;

	/**
	 * Constructor for the CommunicationFunction class. It defines a communication
	 * function among the action in the domain that gives as result the action in
	 * the codomain.
	 * 
	 * @param codomain the result of this communication function
	 * @param domain   the array of actions that should communicate among each other
	 */
	public CommunicationFunction(Action codomain, Action... domain) {
		for (int i = 0; i < domain.length; i++)
			this.domain.add(domain[i]);
		this.codomain = codomain;
	}

	/**
	 * Another constructor for the CommunicationFunction class. It defines a
	 * function s.t. the domain action communicates with n-1 action equal to it to
	 * generate the codomain. For example given codomain: r , domain: s and N = 3 it
	 * construct,
	 * 
	 * s|s|s -> r
	 * 
	 * @param codomain the result of the communication
	 * @param domain   the action that communicates
	 * @param n        how many action equal to the domain has to communicate
	 *                 togheter
	 */
	public CommunicationFunction(Action codomain, Action domain, int n) {
		for(int i=0; i<n ; i++)
			this.domain.add(domain);
		this.codomain = codomain;
		
	}

	/**
	 * {@inheritDoc} It defines how a CommunicationFunction object is printed. That
	 * is a1|...|a_n-> r where a_1...a_n are actions in the domain set and 2 is the codomain action
	 */
	@Override
	public String toString() {
		String s = "";
		Iterator<Action> it = domain.iterator();
		int n = 0;
		while (it.hasNext()) {
			Action a = it.next();
			s = s.concat(a.getId());
			if (n < domain.size() - 1)
				s = s + "|";
			n++;
		}
		s = s + "->" + codomain.getId();
		return s;
	}

}
