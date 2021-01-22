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
 * @see #singleNode is a leafnode of the rpst, so it has no connection with other
 *      nodes 
 * @see #type describes if a node is TRIVIAL or BOND
 * @see #associatedIRPSTNode is a node of the rpst, it has other node/leaf connected
 * 
 * @author S. Belluccini
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
	 * @param f the leaf node
	 * @param type TRIVIAL
	 */
	public ExtendedNode(FlowNode f, TCType type) {
		this.singleNode = f;
		this.type = type;
	}

	/**
	 * Returns the IRPSTNode associated to this node if exits, otherwise null
	 * 
	 * @return  the IRPSTNode associated to this node if exits, otherwise null
	 */
	public IRPSTNode<ControlFlow<FlowNode>, FlowNode> getIRPTNodeAssociated() {
		return this.associatedIRPSTNode;
	}

	/*public Object getTag() {
		if (this.associatedIRPSTNode != null)
			return this.associatedIRPSTNode.getTag();
		else
			return singleNode.getTag();
	}*/

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

	/**
	 * Returns the type of this extended node (i.e. TRIVIAL, BOND)
	 * @return the type of this extended node (i.e. TRIVIAL, BOND)
	 */
	public TCType getType() {
		if (this.associatedIRPSTNode != null)
			return this.associatedIRPSTNode.getType();
		else
			return this.type;
	}

	/**
	 * Returns the name associated to this extended node
	 * @return the name associated to this extended node
	 */
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

	/**
	 * Checks if the given BPMNLabel is equal to the one associated to this extended node
	 * @param l the BPMNLabel
	 * @return true if is equal to the one in the node, false otherwise
	 */
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

	/**
	 * Prints the name of the name (not id) of the node
	 */
	@Override
	public String toString() {
		if (this.associatedIRPSTNode != null)
			return this.associatedIRPSTNode.getName();
		else
			return this.getName();
	}

}
