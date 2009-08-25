package edu.ohio_state.khatchad.refactoring.core;

import java.util.Iterator;

import org.eclipse.jdt.core.IJavaElement;

import edu.ohio_state.khatchad.refactoring.visitor.Visitor;

public class ValuedComputationNode extends ComputationNode {
	private final IJavaElement val;

	public ValuedComputationNode(IJavaElement val) {
		super();
		this.val = val;
	}

	public void accept(Visitor visitor) {
		visitor.visit(this);
		for (final Iterator it = this.children.iterator(); it.hasNext();) {
			final ComputationNode node = (ComputationNode) it.next();
			node.accept(visitor);
		}
	}

	public String getNodeSymbol() {
		return this.val.getElementName();
	}

	public IJavaElement getVal() {
		return this.val;
	}

	public String toString() {
		final StringBuffer ret = new StringBuffer("(" + this.getNodeSymbol()); //$NON-NLS-1$
		for (final Iterator it = this.children.iterator(); it.hasNext();)
			ret.append(it.next());
		ret.append(")"); //$NON-NLS-1$
		return ret.toString();
	}
}