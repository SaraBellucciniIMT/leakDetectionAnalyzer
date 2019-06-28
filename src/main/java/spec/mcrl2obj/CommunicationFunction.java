package spec.mcrl2obj;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/*
 * It's a function s.t a1|...|a_n -> r 
 */
public class CommunicationFunction {
	
	List<String> domain = new ArrayList<String>();
	Action codomain;
	
	public CommunicationFunction(Action codomain,String... domain) {
		for(int i=0; i<domain.length; i++)
			this.domain.add(domain[i]);
		this.codomain = codomain;
	}
	
	public void addActionDomain(String a) {
		this.domain.add(a);
	}

	@Override
	public String toString() {
		String s ="";
		Iterator<String> it = domain.iterator();
		int n= 0;
		while(it.hasNext()) {
			String a = it.next();
			s = s.concat(a);
			if(n<domain.size()-1)
				s = s + "|";
			n++;
		}
		s = s + "->" + codomain.getName();
		return s;
	}
	
	
	
}
