/**
 * 
 */
package spec.mcrl2obj;

import java.util.Set;

import rpstTest.Utils;

/**
 * @author sara
 *
 */
public abstract class AbstractProcess {


	private String name;
	private static final String fiexdName = "P";
	//public static final String id = "id";
	protected Set<Process> insidedef;
	
	protected void setName() {
		name = fiexdName + Utils.getId();
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public abstract String toString();

	public Process getInsideDef(String name) {
		for (Process p : insidedef) {
			if (p.getName().equals(name))
				return p;
		}
		return null;
	}
}
