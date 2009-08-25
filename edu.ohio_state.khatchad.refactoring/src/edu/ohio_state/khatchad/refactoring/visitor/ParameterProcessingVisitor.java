package edu.ohio_state.khatchad.refactoring.visitor;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

import edu.ohio_state.khatchad.refactoring.Messages;
import edu.ohio_state.khatchad.refactoring.exceptions.DefinitelyNotEnumerizableException;
import edu.ohio_state.khatchad.refactoring.exceptions.NonEnumerizableASTException;

public class ParameterProcessingVisitor extends ASTVisitor {
	private final Collection elements = new LinkedHashSet();
	private final Collection expressions = new LinkedHashSet();

	private final int loc;
	private final int paramNumber;

	public ParameterProcessingVisitor(int paramNumber, int loc) {
		this.paramNumber = paramNumber;
		this.loc = loc;
	}

	/**
	 * @return the elements
	 */
	public Collection getElements() {
		return this.elements;
	}

	/**
	 * @return the expressions
	 */
	public Collection getExpressions() {
		return this.expressions;
	}

	public boolean visit(ClassInstanceCreation node) {
		if (node.getType().getStartPosition() == this.loc) {
			final Expression param = (Expression) node.arguments().get(
					this.paramNumber);
			this.expressions.add(param);
		}

		return true;
	}

	public boolean visit(ConstructorInvocation node) {
		if (node.getStartPosition() == this.loc) {
			final Expression param = (Expression) node.arguments().get(
					this.paramNumber);
			this.expressions.add(param);
		}

		return true;
	}

	public boolean visit(MethodDeclaration node) {
		if (node.getName().getStartPosition() == this.loc) {
			final SingleVariableDeclaration svd = (SingleVariableDeclaration) node
					.parameters().get(this.paramNumber);

			final IJavaElement elem = svd.resolveBinding().getJavaElement();
			if (elem.isReadOnly() || svd.getName().resolveBoxing())
				throw new DefinitelyNotEnumerizableException(
						Messages.ASTNodeProcessor_SourceNotPresent, svd);
			if (svd.resolveBinding().getType().isEqualTo(
					node.getAST().resolveWellKnownType("java.lang.Object"))) //$NON-NLS-1$
				throw new NonEnumerizableASTException(Messages.ASTNodeProcessor_IllegalArrayUpcast,
						svd);
			this.elements.add(elem);
		}

		return true;
	}

	public boolean visit(MethodInvocation node) {
		if (node.getName().getStartPosition() == this.loc) {
			final Expression param = (Expression) node.arguments().get(
					this.paramNumber);
			this.expressions.add(param);
		}

		return true;
	}

	public boolean visit(SuperConstructorInvocation node) {
		if (node.getStartPosition() == this.loc) {
			final Expression param = (Expression) node.arguments().get(
					this.paramNumber);
			this.expressions.add(param);
		}

		return true;
	}

	public boolean visit(SuperMethodInvocation node) {
		if (node.getName().getStartPosition() == this.loc) {
			final Expression param = (Expression) node.arguments().get(
					this.paramNumber);
			this.expressions.add(param);
		}

		return true;
	}
}