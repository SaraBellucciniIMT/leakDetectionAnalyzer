/**
 * 
 */
package spec.mcrl2obj;

/**
 * @author sara 
 * Every partecipant has: 1-name 2-personal memory 3-dimension of
 *         the memory;
 */
public class PartecipantProcess extends Process {

	private String namepartecipant;
	private Process memory;
	private int maxdim = 0;
	private Process p;
	private String actionToMemory;
	private String id;
	private Action memoryAction;
	public PartecipantProcess(Process p, String namepartecipant) {
		this.p = p;
		this.namepartecipant = namepartecipant;
		// TODO Auto-generated constructor stub
	}

	public void setActionMemory(Action mem) {
		this.memoryAction = mem;
	}
	
	public Action getActionMemory() {
		return this.memoryAction;
	}
	public String getPartecipantName() {
		return this.namepartecipant;
	}

	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return this.id;
	}
	public void setMemory(Process memory) {
		this.memory = memory;
	}

	public Process getMemory() {
		return this.memory;
	}

	public String getActionToMemory() {
		return this.actionToMemory;
	}

	public String setActionToMemory(String action) {
		return this.actionToMemory = action;
	}

	// Side effect of changing the p partecipant
	public void setProcessPartecipant(Process p) {
		this.p = p;
	}

	public void setMaxDim(int size) {
		this.maxdim = size;
	}
	public Process getProcess() {
		return this.p;
	}

	@Override
	public String getName() {
		return this.p.getName();
	}

	public int getDimensionMemory() {
		return this.maxdim;
	}
	
	

	@Override
	public String toString() {
		String s = p.toString() + "; \n";
		if (memory != null) {
			s = s + memory.getName() + "(m:Memory)= ";
			for (int i = 0; i < memory.getLength(); i++) {
				Process p = memory.getInsideDef(memory.getChildName(i));
				s = s + p.toString();
				if (i != memory.getLength() - 1)
					s = s + memory.getOperator().getValue();
			}
		}
		return s;

	}
}
