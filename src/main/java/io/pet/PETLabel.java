/**
 * 
 */
package io.pet;

/**
 * @author sara
 */
public enum PETLabel {

	SSSHARING("sssharing"), 
	SSCOMPUTATION("sscomputation"),
	SSRECONTRUCTION("ssreconstruction"),
	KENCRYPT("kencrypt"),
	KCOMPUTATION("kcomputation"),
	KDECRYPT("kdecrypt"),
	CIPHER("cipher"),
	PUBLICKEY("publickey"),
	PRIVATEKEY("privatekey"),
	DECODINGKEY("decodingkey"),
	MPC("mpc");

	private final String val;
	
	private PETLabel(String val) {
		this.val = val;
	};
	
	/**
	 * Returns the value of this petlabel. For example the PETLabel SSSHARING as value sssharing
	 * @return the value of this petlabel. For example the PETLabel SSSHARING as value sssharing
	 */
	public String getValue() {
		return val;
	}
	
	/**
	 * Returns the is_value of a petlabel. For example the PETLabel SSSHARING will return is_sssharing
	 * @return the is_value of a petlabel. For example the PETLabel SSSHARING will return is_sssharing
	 */
	public String is_value() {
		return "is_"+val;
	}
}
