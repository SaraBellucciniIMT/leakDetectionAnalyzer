package pcrrlalgo;

import spec.mcrl2obj.Processes.AbstractProcess;
import spec.mcrl2obj.Processes.Process;

/**
 * This is the IParout interface, it defines the method to be implemented by
 * subclasses participating in the parout operation, i.e. bringing the parallel
 * operator as a top level operator in the mCRL2 specification
 * 
 * @author S. Belluccini
 *
 */
public interface IParout {

	AbstractProcess interpreter(Process process);

}
