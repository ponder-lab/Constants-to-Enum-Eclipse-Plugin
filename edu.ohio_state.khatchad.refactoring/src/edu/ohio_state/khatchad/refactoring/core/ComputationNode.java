package edu.ohio_state.khatchad.refactoring.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import edu.ohio_state.khatchad.refactoring.visitor.Visitable;

public abstract class ComputationNode implements Visitable {

	protected List children = new ArrayList();
	protected ComputationNode parent;

	public ComputationNode() {
		super();
	}

	public Collection getAllChildren() {
		final Collection ret = new ArrayList(this.children);
		for (final Iterator it = this.children.iterator(); it.hasNext();) {
			final ComputationNode child = (ComputationNode) it.next();
			ret.addAll(child.getAllChildren());
		}
		return ret;
	}

	public List getChildren() {
		return this.children;
	}

	public Collection getComputationTreeElements() {
		// Get all the nodes of the tree.
		final Collection family = this.getAllChildren();
		family.add(this);

		// Return the elements corresponding to those nodes.
		final Collection ret = new ArrayList();
		for (final Iterator it = family.iterator(); it.hasNext();) {
			final ComputationNode member = (ComputationNode) it.next();
			if (member instanceof ValuedComputationNode)
				ret.add(((ValuedComputationNode) member).getVal());
		}

		return ret;
	}

	public abstract String getNodeSymbol();

	public ComputationNode getRoot() {
		ComputationNode trav = this;
		ComputationNode last = trav;
		while (trav != null) {
			last = trav;
			trav = trav.parent;
		}
		return last;
	}

	public void makeParent(ComputationNode node) {
		node.parent = this;
		this.children.add(node);
	}
}