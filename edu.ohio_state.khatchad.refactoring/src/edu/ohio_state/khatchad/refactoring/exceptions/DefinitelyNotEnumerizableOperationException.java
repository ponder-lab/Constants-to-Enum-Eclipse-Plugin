package edu.ohio_state.khatchad.refactoring.exceptions;

import org.eclipse.jdt.core.dom.ASTNode;

public class DefinitelyNotEnumerizableOperationException extends
		DefinitelyNotEnumerizableException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7103865114397614847L;

	private final Object operator;

	public DefinitelyNotEnumerizableOperationException(String message,
			Object op, ASTNode problem) {
		super(message, problem);
		this.operator = op;
	}

	public String toString() {
		final StringBuffer ret = new StringBuffer(super.toString());
		ret.delete(ret.length() - 3, ret.length());
		ret.append("\t" + this.operator);
		ret.append("\t\t");
		return ret.toString();
	}

}
