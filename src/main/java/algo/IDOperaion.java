package algo;

public enum IDOperaion {

	TASK(1),
	PARTICIPANT(2),
	SSSHARING(3),
	RECONSTRUCTION(4);
	
	private final int i;
	IDOperaion(int i) {
		this.i = i;
	}
	
	public int getVal() {
		return i;
	}
}
