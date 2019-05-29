package spec;

import java.util.Set;

import spec.mcrl2obj.Action;
import spec.mcrl2obj.Sort;

/*
 * Class that define a mcrl2 spec object 
 */
public class mCRL2 implements ISpec{

	Sort sort;
	Set<Action> actions;
	Set<Action> allow ;
	Set<Action> comm;
	Set<Action> hide;
	Set<Process> processes;
	Set<Process> initSet;
	
	public mCRL2() {
	}

	
	public Sort getSort() {
		return sort;
	}

	public void setSort(Sort sort) {
		this.sort = sort;
	}

	public Set<Action> getActions() {
		return actions;
	}

	public void setActions(Set<Action> actions) {
		this.actions = actions;
	}

	public Set<Action> getAllow() {
		return allow;
	}

	public void setAllow(Set<Action> allow) {
		this.allow = allow;
	}

	public Set<Action> getComm() {
		return comm;
	}

	public void setComm(Set<Action> comm) {
		this.comm = comm;
	}

	public Set<Action> getHide() {
		return hide;
	}

	public void setHide(Set<Action> hide) {
		this.hide = hide;
	}

	public Set<Process> getProcesses() {
		return processes;
	}

	public void setProcesses(Set<Process> processes) {
		this.processes = processes;
	}

	public Set<Process> getInitSet() {
		return initSet;
	}

	public void setInitSet(Set<Process> initSet) {
		this.initSet = initSet;
	}
	
	
	
}