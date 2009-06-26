package edu.ohio_state.khatchad.refactoring.core;

import java.util.Iterator;

import edu.ohio_state.khatchad.refactoring.visitor.Visitor;

public class UnionComputationNode extends ComputationNode {
	public void accept(Visitor visitor) {
		visitor.visit(this);
		for (final Iterator it = this.children.iterator(); it.hasNext();) {
			final ComputationNode node = (ComputationNode) it.next();
			node.accept(visitor);
		}
	}

	public String getNodeSymbol() {
		return "<UNION>";
	}

	public String toString() {
		final StringBuffer ret = new StringBuffer("(" + this.getNodeSymbol());
		for (final Iterator it = this.children.iterator(); it.hasNext();)
			ret.append(it.next());
		ret.append(")");
		return ret.toString();
	}
}
