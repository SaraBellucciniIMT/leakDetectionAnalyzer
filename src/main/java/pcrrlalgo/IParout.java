package pcrrlalgo;

import pcrrlalgoelement.Parout;
import spec.mcrl2obj.Process;

public interface IParout {

	
	void interpreter(Parout process, Process dada, Process child);

}
