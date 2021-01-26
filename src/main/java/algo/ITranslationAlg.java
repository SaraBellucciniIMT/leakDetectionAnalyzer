package algo;


import spec.ISpec;

/**
 * Interface for every translation algorithm
 * 
 * @see #getSpec() every traslation algorithm should return an ISort object
 * 
 * @author S. Belluccini
 *
 */
public interface ITranslationAlg {
	

	/**
	 * Returns a formal specification of some kind
	 * @return a formal specification of some kind
	 */
	public ISpec getSpec();
}
