package sort;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Sets;


/**
 * This is the Collection class that represent the sort collection in the mCRL2
 * specification
 * 
 * @author S. Belluccini
 *
 */
public class Collection implements ISort,Iterable<ISort>  {

	/**
	 * The set of data in this collection
	 */
	private static final String NAMESORT = "Collection";
	private Set<ISort> collection;

	/**
	 * Constructor for the Collection class, it instantiate the collection set
	 */
	public Collection() {
		this.collection = new HashSet<ISort>();
	}

	/**
	 * Another constructor for the Collection class, it assign the input set to the
	 * collection set
	 * 
	 * @param data set that we want to assign
	 */
	public Collection(Set<ISort> data) {
		this.collection = data;
	}

	/**
	 * Adds a data to this set
	 * 
	 * @param d the data
	 */
	public void addData(ISort... d) {
		this.collection.addAll(Sets.newHashSet(d));
	}

	public ISort[] getElements() {
		return this.collection.toArray(new ISort[this.collection.size()]);
	}

	public int size() {
		return this.collection.size();
	}

	public boolean isEmpty() {
		if (collection.isEmpty())
			return true;
		else
			return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String string = "{";
		if (!this.collection.isEmpty()) {
			int i = 0;
			for (ISort s : collection) {
				string += s.toString();
				if (i != collection.size() - 1)
					string += ",";
				i++;
			}
		}
		return string + "}";
	}

	/**
	 * Return the name of this sort
	 * 
	 * @return the name of this sort
	 */
	public static String nameSort() {
		return "Collection";
	}

	public String toStringSort() {
		return NAMESORT + " = Set(" + Data.nameSort() + ");";
	}

	@Override
	public String getNameSort() {
		return NAMESORT;
	}

	@Override
	public Iterator<ISort> iterator() {
		return this.collection.iterator();
	}

}
