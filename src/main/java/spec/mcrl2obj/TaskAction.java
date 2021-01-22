package spec.mcrl2obj;

import io.pet.AbstractTaskPET;
import sort.Collection;
import sort.Data;
import sort.ISort;

public class TaskAction extends Action {

	private Collection param_collection = new Collection();

	public TaskAction(String realName, String id) {
		super(realName, id);
	}

	/**
	 * Another constructor for the Action class
	 * 
	 * @param realName the real name of the action
	 * @param id       the associated id
	 * @param pet      the pet associated to this action
	 */
	public TaskAction(String realName, String id, AbstractTaskPET pet) {
		super(realName, id, pet);
	}

	/**
	 * Another constructor for Action with parameters, where realName is the
	 * realName and id of the action
	 * 
	 * @param realName       and id of the action
	 * @param dataParameters contained by this action
	 */
	public TaskAction(String realName, ISort... dataParameters) {
		super(realName, realName);
		if (dataParameters.length != 0) 
			this.param_collection.addData(dataParameters);
		
	}
	
	@Override
	public String printActionType() {
		return Collection.nameSort();
	}

	@Override
	public void addParameters(ISort... datas) {
		if (this.param_collection == null)
			this.param_collection = new Collection();
		this.param_collection.addData(datas);	
	}
	
	/**
	 * Checks if a parameter with that real name is inside this collection
	 * @param realName the realname of the parameter
	 * @return the parameter if exists, otherwise null
	 */
	public ISort contain(String realName) {
		if(!param_collection.isEmpty()) {
			for(ISort p : param_collection) {
				if(p.getClass().equals(Data.class) && ((Data)p).getRealName().equals(realName))
					return p;
			}
		}
		return null;
	}

	/**
	 * Returns the number of the parameters inside the collection
	 * @return the number of the parameters inside the collection
	 */
	public int size() {
		return this.param_collection.size();
	}

	@Override
	public ISort[] getParameters() {
		return this.param_collection.getElements();
	}

	@Override
	public String toString() {
		String st="";
		if(id!= null) {
			st = id + "(" + param_collection.toString() + ")";
		}
		return st;
	}
}
