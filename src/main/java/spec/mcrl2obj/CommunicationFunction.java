package spec.mcrl2obj;

import java.util.Iterator;
import java.util.Set;


/*
 * It's a function s.t a1|...|a_n -> r 
 */
public class CommunicationFunction {
	
	Set<Action> domain ;
	Action codomain;
	
	public CommunicationFunction(Set<Action> domain, Action codomain) {
		this.domain = domain;
		this.codomain = codomain;
	}
	
	public void addActionDomain(Action a) {
		this.domain.add(a);
	}

	@Override
	public String toString() {
		String s ="";
		Iterator<Action> it = domain.iterator();
		int n= 0;
		while(it.hasNext()) {
			Action a = it.next();
			s = s.concat(a.getName());
			if(n<domain.size()-1)
				s = s + "|";
			n++;
		}
		s = s + "->" + codomain.getName();
		return s;
	}
	
	
	
}
