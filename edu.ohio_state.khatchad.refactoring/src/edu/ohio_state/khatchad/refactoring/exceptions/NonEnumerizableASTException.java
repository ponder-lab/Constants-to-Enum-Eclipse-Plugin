package edu.ohio_state.khatchad.refactoring.exceptions;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class NonEnumerizableASTException extends NonEnumerizableException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1668833316083844951L;

	private final ASTNode problem;

	public NonEnumerizableASTException(String message, ASTNode problem) {
		super(message);
		this.problem = problem;
	}

	public String toString() {
		final CompilationUnit root = (CompilationUnit) this.problem.getRoot();
		final ICompilationUnit icu = (ICompilationUnit) root.getJavaElement();

		final StringBuffer ret = new StringBuffer();

		ret.append(icu.getJavaProject().getProject().getName());
		ret.append("\t"); //$NON-NLS-1$

		ret.append(root.getPackage() != null ? root.getPackage().getName()
				+ "." + icu.getElementName() : icu.getElementName()); //$NON-NLS-1$

		ret.append("\t"); //$NON-NLS-1$
		ret.append(root.getLineNumber(this.problem.getStartPosition()));
		ret.append("\t"); //$NON-NLS-1$
		ret.append(this.problem.getStartPosition());
		ret.append("\t"); //$NON-NLS-1$
		ret.append(this.problem.getLength());
		ret.append("\t"); //$NON-NLS-1$
		ret.append(this.getClass().getName());
		ret.append("\t"); //$NON-NLS-1$
		ret.append(this.getMessage());
		ret.append("\t"); //$NON-NLS-1$
		ret.append(ASTNode.nodeClassForType(this.problem.getNodeType())
				.getName());
		ret.append("\t"); //$NON-NLS-1$
		ret.append(this.problem.toString().replace('\n', ' '));
		ret.append("\t\t\t"); //$NON-NLS-1$

		return ret.toString();
	}
}
