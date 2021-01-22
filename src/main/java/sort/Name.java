package sort;

import java.util.HashMap;
import java.util.Map;

import rpstTest.Utils;
import spec.mcrl2obj.MCRL2;

/**
 * Name is the class of data object inside BPMN model
 */
public class Name implements ISort {

	private static Map<String, String> setName = new HashMap<String, String>();

	/**
	 * The id represent the data object inside the specification
	 */
	private String id;
	/**
	 * Real name of the data object inside the collaboration
	 */
	private String realName;

	public Name() {
		this.id = null;
		this.realName = null;
	}

	/**
	 * Constructor for Name class, an id to represent this object is assigned
	 * 
	 * @param realName the real name of the data object
	 */
	public Name(String realName) {
		this.realName = realName;
		if (setName.containsKey(realName))
			this.id = setName.get(realName);
		else {
			this.id = MCRL2.getDataName();
			setName.put(realName, this.id);
		}

	}

	public Name(String id, String realName) {
		this.id = id;
		this.realName = realName;

	}

	/**
	 * Returns the id that identifies this object
	 * 
	 * @return the id that identifies this object
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Returns the real name of a data object
	 * 
	 * @return the real name of a data object
	 */
	public String getRealName() {
		return this.realName;
	}

	/**
	 * Gives the id that identifies this object
	 * 
	 * @return the id that identifies this object
	 */
	@Override
	public String toString() {
		return getId();
	}

	public String toStringSort() {
		return Utils.printAsStruct(nameSort(), setName.values().toArray(new String[setName.size()]));
	}

	public static String nameSort() {
		return "Name";
	}

	@Override
	public String getNameSort() {
		return nameSort();
	}

}
