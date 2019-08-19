package pcrrlalgoelement;

import java.util.ArrayList;
import java.util.List;

import spec.mcrl2obj.Process;

public class Parallel extends AbstractParaout{

	@Override
	public void interpreter(Parout parout, Process dad, Process child) {
		// TODO Auto-generated method stub
		
		List<String> newchild = new ArrayList<String>();
		
		dad.getChild().forEach(c->{if(!c.equals(child.getName())) newchild.add(c);});
		child.getChild().forEach(c->newchild.add(c));
		child.getAllInsideDef().forEach(i->dad.addInsideDef(i));
		dad.setChild(newchild);
		parout.removeProcess(child.getName());
	}

}
