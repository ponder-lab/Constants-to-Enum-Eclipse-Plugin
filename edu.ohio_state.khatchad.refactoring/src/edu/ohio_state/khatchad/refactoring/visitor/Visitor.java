package edu.ohio_state.khatchad.refactoring.visitor;

import edu.ohio_state.khatchad.refactoring.core.ComputationNode;
import edu.ohio_state.khatchad.refactoring.core.UnionComputationNode;
import edu.ohio_state.khatchad.refactoring.core.ValuedComputationNode;

public interface Visitor {
	public void visit(ComputationNode node);

	public void visit(UnionComputationNode node);

	public void visit(ValuedComputationNode node);
}
