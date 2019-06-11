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
	private String id ="";
	
	
	protected void setName() {
		name = fiexdName + (i++);
	}
	
	public String getName() {
		return name;
	}
	
	public void setId(String name) {
		id = name;
	}
	
	public String getId() {
		return id;
	}
	@Override
	public abstract String toString();
}
