package io;

import org.jbpt.algo.tree.rpst.IRPSTNode;
import org.jbpt.algo.tree.tctree.TCType;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;

public class ExtendedNode {

	private FlowNode singleNode;
	private Object tag;
	private TCType type;
	private String id;
	private IRPSTNode<ControlFlow<FlowNode>, FlowNode> associatedIRPSTNode;
	
	public ExtendedNode(IRPSTNode<ControlFlow<FlowNode>, FlowNode> f) {
		this.associatedIRPSTNode = f;
	}

	/*
	 * It's a node only with an entry to represent a single element like STARTEVENT,
	 * TASK, ENDEVENT
	 */
	public ExtendedNode(FlowNode entry, Object tag, TCType type, String id) {
		this.singleNode = entry;
		this.id = id;
		this.tag = tag;
		this.type = type;
	}

	public IRPSTNode<ControlFlow<FlowNode>, FlowNode> getIRPTNodeAssociated() {
		return this.associatedIRPSTNode;
	}

	public Object getTag() {
		if (this.associatedIRPSTNode != null)
			return this.associatedIRPSTNode.getTag();
		else
			return this.tag;
	}

	public String getId() {
		if (this.associatedIRPSTNode != null)
			return this.associatedIRPSTNode.getId();
		else
			return this.id;
	}

	public TCType getType() {
		if (this.associatedIRPSTNode != null)
			return this.associatedIRPSTNode.getType();
		else
			return this.type;
	}

	public String getName() {
		if (this.associatedIRPSTNode != null)
			return this.associatedIRPSTNode.getName();
		else
			return this.singleNode.getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((singleNode == null) ? 0 : singleNode.hashCode());
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExtendedNode other = (ExtendedNode) obj;
		if (this.associatedIRPSTNode == null) {
			if (singleNode == null) {
				if (other.singleNode != null)
					return false;
			} else if (!singleNode.equals(other.singleNode))
				return false;
			if (tag == null) {
				if (other.tag != null)
					return false;
			} else if (!tag.equals(other.tag))
				return false;
			if (type != other.type)
				return false;
			return true;
		} else {
			if(this.associatedIRPSTNode.equals(other.getIRPTNodeAssociated()))
				return true;
			else
				return false;
		}
	}

	@Override
	public String toString() {
		if (this.associatedIRPSTNode != null)
			return this.associatedIRPSTNode.getName();
		else
			return this.getId();
	}

}
