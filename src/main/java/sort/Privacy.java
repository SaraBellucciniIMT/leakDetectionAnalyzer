package sort;

public class Privacy extends AbstractStructSort{

	private PName pname;
	private int i ;
	
	public Privacy() {
		this.type.add("pair(frt:PName,snd:Nat)");
	}

	public Privacy(PName pname, int i) {
		this.pname = pname;
		this.i = i;
	}
	@Override
	public String getDefinition() {
		return getClass().getName() + printStruct();
	}

	@Override
	public String toString() {
		return "pair("+pname.toString()+","+ i+")";
	}

}
