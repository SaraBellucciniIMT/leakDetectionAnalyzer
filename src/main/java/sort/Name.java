package sort;

import java.util.Set;

import rpstTest.Utils;

/*
 * Basic sort to represent data objects inside the specification
 * -name: is the name of the data object coming from the BPMN model
 * -id : is the name that the data object will assume in the specifcation id order to NOT violate 
 */
public class Name extends AbstractStructSort{

	private static Set<String> dataparameters;
	private String id;
	private String name;
	

	@Override
	public String getDefinition() {
		this.type = dataparameters;
		return this.getClass().getName() + printStruct();
	}
	
	
	public Name(String n) {
		this.name = n;
		this.id = Utils.getDataID();
		Name.dataparameters.add(id.toString());
	}
	
	public String getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}
	
	@Override
	public String toString() {
		return getId();
	}

}
