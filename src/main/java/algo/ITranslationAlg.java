package algo;


import spec.ISpec;

/**
 * Interface for every translation algorithm
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
