/**
 * 
 */
package spec.mcrl2obj;

/**
 * @author sara
 *
 */
public abstract class AbstractProcess {


	private String name;
	private static int i =0;
	private static final String fiexdName = "P";
	
	
	protected void setName() {
		name = fiexdName + (i++);
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public abstract String toString();
}