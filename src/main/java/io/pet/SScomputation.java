/**
 * 
 */
package io.pet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author sara
 *
 */
public class SScomputation extends PET{

	private Map<String,List<String>> map = new HashMap<String,List<String>>();
	
	public SScomputation(Map<String,List<String>> map) {
		// TODO Auto-generated constructor stub
		this.petname = PETLabel.SSCOMPUTATION;
		this.map = map;
	}
	
	public void addSScomputationGroup(String groupid,List<String> objref) {
		// TODO Auto-generated constructor stub
		this.map.put(groupid, objref);
	}
	
	public Map<String,List<String>> getGroups(){
		return this.map;
	}
	@Override
	public PETLabel getPET() {
		// TODO Auto-generated method stub
		return this.petname;
	}
	
	
	public void changeMyName(String myname,String myid) {
		for(Entry<String,List<String>> entry : map.entrySet()) {
			for(String s : entry.getValue()) {
				if(s.equals(myid)) {
					entry.getValue().remove(s);
					entry.getValue().add(adjustname(myname));
					return;
				}
			}
		}
		
	}
	
	private String adjustname(String s) {
		if(s.contains(" "))
			s = s.replaceAll(" ","_");
		return s;
	}
	public boolean containObjRef(String s) {
		for(Entry<String,List<String>> entry : map.entrySet()) {
			if(entry.getValue().contains(s))
				return true;
		}
		return false;
	}
	
	
}
