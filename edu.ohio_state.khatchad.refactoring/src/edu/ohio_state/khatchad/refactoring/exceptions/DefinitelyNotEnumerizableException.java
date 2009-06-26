package edu.ohio_state.khatchad.refactoring.exceptions;

import org.eclipse.jdt.core.dom.ASTNode;

public class DefinitelyNotEnumerizableException extends
		NonEnumerizableASTException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4408046040696438295L;

	public DefinitelyNotEnumerizableException(String message, ASTNode problem) {
		super(message, problem);
	}

}
