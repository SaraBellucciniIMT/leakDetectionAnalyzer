package sort;

import spec.mcrl2obj.MCRL2Utils;

/**
 * It defines all the methods that should be implemented by a ISort type
 * 
 * @author S. Belluccini
 *
 */
public interface ISort {
	
	/**
	 * Eps data is the empty data
	 */
	public static final String BOOL = "Bool";
	public static final Placeholder TRUE = new Placeholder("true", BOOL);
	public static final Placeholder FALSE = new Placeholder("false", BOOL);
	public static final String NAT = "Nat";
	public static final String BAG_NAT = MCRL2Utils.printf("Bag", NAT);
	
	/**
	 * Returns a string representing how the sort is printed in the proc part of the
	 * specification in mCRL2
	 * 
	 * @return a string representing how the sort is printed in the proc part of the
	 *         specification in mCRL2
	 */
	public String toString();

	/**
	 * Returns a string representing how the sort is printed in the sort part of the
	 * mCRL2 specification
	 * 
	 * @return a string representing how the sort is printed in the sort part of the
	 *         mCRL2 specification
	 */
	public String toStringSort();
	
	public String getNameSort();
}
