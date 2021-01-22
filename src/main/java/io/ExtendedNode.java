package io;

import org.jbpt.algo.tree.rpst.IRPSTNode;
import org.jbpt.algo.tree.tctree.TCType;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;

import io.pet.AbstractTaskPET;

/**
 * The extended nodes are an extension of the flow node in the RPST tree. Are
 * use to keep more information about a node like:
 * 
 * @see #singleNode is a single flow node, that has no connection with other
 *      nodes
 * @see #tag is the type of
 * @see #type
 * @see #id
 * @see #associatedIRPSTNode
 * @author Sara
 *
 */
public class ExtendedNode {

	private FlowNode singleNode;
	private TCType type;
	private IRPSTNode<ControlFlow<FlowNode>, FlowNode> associatedIRPSTNode;

	/**
	 * Constructor used in case of BOND and RIGID TCTtype since they have children
	 * nodes
	 * 
	 * @param f the subtree that has a bond or rigid has root
	 */
	public ExtendedNode(IRPSTNode<ControlFlow<FlowNode>, FlowNode> f) {
		this.associatedIRPSTNode = f;
	}

	/**
	 * Constructor used in case of leaf nodes like TRIVIALS
	 * 
	 * @param f
	 * @param type
	 */
	public ExtendedNode(FlowNode f, TCType type) {
		this.singleNode = f;
		this.type = type;
	}

	public IRPSTNode<ControlFlow<FlowNode>, FlowNode> getIRPTNodeAssociated() {
		return this.associatedIRPSTNode;
	}

	public Object getTag() {
		if (this.associatedIRPSTNode != null)
			return this.associatedIRPSTNode.getTag();
		else
			return singleNode.getTag();
	}

	/**
	 * Returns the ID that identifies this IRPSTNode or single node
	 * 
	 * @return the ID that identifies this IRPSTNode or single node
	 */
	public String getId() {
		if (this.associatedIRPSTNode != null)
			return this.associatedIRPSTNode.getId();
		else
			return this.singleNode.getId();
	}

	public String getDescription() {
		if (this.associatedIRPSTNode != null)
			return this.associatedIRPSTNode.getDescription().toString();
		else
			return this.singleNode.getDescription();
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

	/**
	 * Returns the AbstractTaskPET attached to the single node, if exists
	 * 
	 * @return the AbstractTaskPET attached to the single node, if exists
	 */
	public AbstractTaskPET getPet() {
		if (this.singleNode != null && this.singleNode.getTag() != null)
			return (AbstractTaskPET) this.singleNode.getTag();
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((singleNode == null) ? 0 : singleNode.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	public boolean equalsDescription(BPMNLabel l) {
		if (this.associatedIRPSTNode != null && ((this.associatedIRPSTNode.getDescription() != null
				&& this.associatedIRPSTNode.getDescription().equals(l.name()))
				|| (this.associatedIRPSTNode.getEntry() != null
						&& this.associatedIRPSTNode.getEntry().getDescription() != null
						&& this.associatedIRPSTNode.getEntry().getDescription().equals(l.name()))))
			return true;
		else if (this.singleNode != null && this.singleNode.getDescription().equals(l.name()))
			return true;
		return false;
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
			if (type != other.type)
				return false;
			return true;
		} else {
			if (this.associatedIRPSTNode.equals(other.getIRPTNodeAssociated()))
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
			return this.getName();
	}

}
