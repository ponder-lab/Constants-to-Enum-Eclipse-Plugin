package edu.ohio_state.khatchad.refactoring.visitor;

import java.util.Collection;

import org.eclipse.jdt.core.IJavaElement;

import edu.ohio_state.khatchad.refactoring.core.ComputationNode;
import edu.ohio_state.khatchad.refactoring.core.UnionComputationNode;
import edu.ohio_state.khatchad.refactoring.core.ValuedComputationNode;

public class TreeTrimingVisitor implements Visitor {
	private final Collection computationForest;
	private final Collection nonEnumerizableList;

	public TreeTrimingVisitor(Collection computationForest,
			Collection nonEnumerizableList) {
		this.nonEnumerizableList = nonEnumerizableList;
		this.computationForest = computationForest;
	}

	public void visit(ComputationNode node) {
	}

	public void visit(UnionComputationNode node) {
	}

	public void visit(ValuedComputationNode node) {
		final IJavaElement extractedValue = node.getVal();
		if (this.nonEnumerizableList.contains(extractedValue))
			this.computationForest.remove(node.getRoot());
	}
}