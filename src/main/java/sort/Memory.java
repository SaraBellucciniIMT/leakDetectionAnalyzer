package sort;

import java.util.ArrayList;
import java.util.List;

public class Memory implements ISort {
	private List<Data> memory;

	public Memory() {
		this.memory = new ArrayList<Data>();
	}

	public Memory(List<Data> l) {
		this.memory = l;
	
	}

	public void addData(Data d) {
		this.memory.add(d);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String string = "[";
		for (int i = 0; i < memory.size(); i++) {
			string += memory.get(i).toString();
			if (i != memory.size() - 1)
				string += ",";
		}
		return string + "]";
	}

	public String toStringSort() {
		return nameSort() + " = List(" + Data.nameSort()+ ");";
	}

	public static String nameSort() {
		return "Memory";
	}

	@Override
	public String getNameSort() {
		return Memory.nameSort();
	}
	
	
}
