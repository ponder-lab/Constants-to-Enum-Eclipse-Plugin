package edu.ohio_state.khatchad.refactoring.exceptions;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class NonEnumerizableCastExpression extends NonEnumerizableASTException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5755481244378033689L;

	private final ITypeBinding fromType;
	private final ITypeBinding toType;

	public NonEnumerizableCastExpression(String message, ASTNode problem,
			ITypeBinding fromType, ITypeBinding toType) {
		super(message, problem);
		this.fromType = fromType;
		this.toType = toType;
	}

	public String toString() {
		final StringBuffer ret = new StringBuffer(super.toString());
		ret.delete(ret.length() - 1, ret.length());
		ret.append(this.fromType.getQualifiedName());
		ret.append('\t');
		ret.append(this.toType.getQualifiedName());
		return ret.toString();
	}
}
