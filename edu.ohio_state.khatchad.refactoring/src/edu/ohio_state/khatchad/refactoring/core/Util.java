package edu.ohio_state.khatchad.refactoring.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.internal.corext.refactoring.rename.MethodChecks;

import edu.ohio_state.khatchad.refactoring.exceptions.BinaryElementEncounteredException;
import edu.ohio_state.khatchad.refactoring.visitor.TreeTrimingVisitor;

/**
 * Various utility stuff.
 */
public class Util {

	public static Collection extractLegalInfixExpressionsInNeedOfTransformation(
			Collection col) {
		final Collection ret = new LinkedHashSet();
		for (final Iterator it = col.iterator(); it.hasNext();) {
			final InfixExpression ie = (InfixExpression) it.next();
			if (inNeedOfTransformation(ie.getOperator()))
				ret.add(ie);
		}
		return ret;
	}

	public static Map extractLegalInfixExpressionsInNeedOfTransformation(Map map) {
		final Map ret = new LinkedHashMap(map);
		for (final Iterator it = ret.keySet().iterator(); it.hasNext();) {
			final IJavaElement elem = (IJavaElement) it.next();
			final Collection validExp = extractLegalInfixExpressionsInNeedOfTransformation((Collection) ret
					.get(elem));
			if (validExp.isEmpty())
				it.remove();
			else
				((Collection) ret.get(elem)).retainAll(validExp);
		}

		return ret;
	}

	public static Collection flattenForest(Collection forest) {
		final Collection ret = new LinkedHashSet();
		for (final Iterator it = forest.iterator(); it.hasNext();) {
			final Collection set = (Collection) it.next();
			ret.addAll(set);
		}
		return ret;
	}

	public static ASTNode getASTNode(IJavaElement elem, IProgressMonitor monitor) {
		final IMember mem = getIMember(elem);
		final ICompilationUnit icu = mem.getCompilationUnit();
		if (icu == null)
			throw new BinaryElementEncounteredException("Source not present.",
					mem);
		final ASTNode root = Util.getCompilationUnit(icu, monitor);
		return root;
	}

	public static CompilationUnit getCompilationUnit(ICompilationUnit icu,
			IProgressMonitor monitor) {
		final ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(icu);
		parser.setResolveBindings(true);
		final CompilationUnit ret = (CompilationUnit) parser.createAST(monitor);
		return ret;
	}

	public static Collection getConsistentlyVisibleSets(Collection elementSets)
			throws JavaModelException {
		final Collection ret = new LinkedHashSet(elementSets);
		for (final Iterator it = elementSets.iterator(); it.hasNext();) {
			final Collection set = (Collection) it.next();

			boolean allPublic = true;
			boolean allPrivate = true;
			boolean allPackage = true;
			boolean allProtected = true;

			for (final Iterator jit = set.iterator(); jit.hasNext();) {
				final IJavaElement elem = (IJavaElement) jit.next();
				if (elem.getElementType() == IJavaElement.FIELD) {
					final IField field = (IField) elem;
					final Object constValue = field.getConstant();
					if (constValue != null) {
						allPublic &= Flags.isPublic(field.getFlags());
						allPrivate &= Flags.isPrivate(field.getFlags());
						allProtected &= Flags.isProtected(field.getFlags());
						allPackage &= Flags.isPackageDefault(field.getFlags());
					}
				}
			}
			if (!(allPublic || allPrivate || allPackage || allProtected))
				ret.remove(set);
		}
		return ret;
	}

	public static int getConsistentVisibility(Collection col)
			throws JavaModelException {
		if (col.isEmpty())
			return -1;

		// They should already be consistent.
		final IMember firstElem = (IMember) col.iterator().next();
		if (Flags.isPublic(firstElem.getFlags()))
			return Flags.AccPublic;
		else if (Flags.isPackageDefault(firstElem.getFlags()))
			return Flags.AccDefault;
		else if (Flags.isPrivate(firstElem.getFlags()))
			return Flags.AccPrivate;
		else if (Flags.isProtected(firstElem.getFlags()))
			return Flags.AccProtected;
		else
			throw new IllegalArgumentException(
					"Members are not of a valid visibility.");
	}

	public static Collection getDistinctSets(Collection elementSets)
			throws JavaModelException {
		final Collection ret = new LinkedHashSet(elementSets);
		for (final Iterator it = elementSets.iterator(); it.hasNext();) {
			final Collection set = (Collection) it.next();
			final Collection constValues = new ArrayList();
			for (final Iterator jit = set.iterator(); jit.hasNext();) {
				final IJavaElement elem = (IJavaElement) jit.next();
				if (elem.getElementType() == IJavaElement.FIELD) {
					final IField field = (IField) elem;
					final Object constValue = field.getConstant();
					if (constValue != null)
						constValues.add(constValue);
				}
			}

			if (!distinct(constValues))
				ret.remove(set);
		}
		return ret;
	}

	public static Collection getElementForest(Collection computationForest) {
		final Collection ret = new LinkedHashSet();
		for (final Iterator it = computationForest.iterator(); it.hasNext();) {
			final ComputationNode tree = (ComputationNode) it.next();
			ret.add(tree.getComputationTreeElements());
		}
		return ret;
	}

	public static ASTNode getExactASTNode(CompilationUnit root,
			final SearchMatch match) {
		final ArrayList ret = new ArrayList(1);
		final ASTVisitor visitor = new ASTVisitor() {
			public void preVisit(ASTNode node) {
				if (node.getStartPosition() == match.getOffset()) {
					ret.clear();
					ret.add(node);
				}
			}
		};
		root.accept(visitor);
		return (ASTNode) ret.get(0);
	}

	public static ASTNode getExactASTNode(IJavaElement elem,
			final SearchMatch match, IProgressMonitor monitor) {
		final IMember mem = getIMember(elem);
		final CompilationUnit root = Util.getCompilationUnit(mem
				.getCompilationUnit(), monitor);
		return getExactASTNode(root, match);
	}

	public static ASTNode getExactASTNode(SearchMatch match,
			IProgressMonitor monitor) {
		final IJavaElement elem = (IJavaElement) match.getElement();
		return Util.getExactASTNode(elem, match, monitor);
	}

	public static FieldDeclaration getFieldDeclaration(ASTNode node) {
		if (node == null)
			return null;
		else if (node instanceof FieldDeclaration)
			return (FieldDeclaration) node;
		else
			return getFieldDeclaration(node.getParent());
	}

	public static IMember getIMember(IJavaElement elem) {

		if (elem == null)
			throw new IllegalArgumentException(
					"Can not get IMember from null element.");

		switch (elem.getElementType()) {
		case IJavaElement.METHOD:
		case IJavaElement.FIELD:
		case IJavaElement.INITIALIZER:
		case IJavaElement.TYPE: {
			return (IMember) elem;
		}
		}

		return getIMember(elem.getParent());
	}

	public static InfixExpression getInfixExpression(ASTNode node) {
		if (node == null)
			return null;
		else if (node instanceof InfixExpression)
			return (InfixExpression) node;
		else
			return getInfixExpression(node.getParent());
	}

	public static MethodDeclaration getMethodDeclaration(ASTNode node) {
		ASTNode trav = node;
		while (trav.getNodeType() != ASTNode.METHOD_DECLARATION)
			trav = trav.getParent();
		return (MethodDeclaration) trav;
	}

	public static SingleVariableDeclaration getSingleVariableDeclaration(
			ASTNode node) {
		if (node == null)
			return null;
		else if (node instanceof SingleVariableDeclaration)
			return (SingleVariableDeclaration) node;
		else
			return getSingleVariableDeclaration(node.getParent());
	}

	public static Name getTopmostName(ASTNode node) {
		if (node == null)
			return null;
		else if (node.getParent() == null
				|| node.getParent().getNodeType() != ASTNode.QUALIFIED_NAME)
			return (Name) node;
		else
			return getTopmostName(node.getParent());
	}

	/**
	 * Returns the top most source method of the argument. Returns null if the
	 * method argument overrides a binary method.
	 * 
	 * @param meth
	 *            The method to retrieve the topmost method of.
	 * @return The topmost source method or null if the topmost method is not
	 *         from source.
	 * @throws JavaModelException
	 *             From framework.
	 */
	public static IMethod getTopMostSourceMethod(IMethod meth,
			IProgressMonitor monitor) throws JavaModelException {
		IMethod top = MethodChecks.isVirtual(meth) ? MethodChecks
				.getTopmostMethod(meth, meth.getDeclaringType()
						.newSupertypeHierarchy(monitor), monitor) : meth;

		if (top == null)
			top = meth;

		if (top.isBinary())
			return null;
		else
			return top;
	}

	public static Collection getUniquelyNamedSets(Collection elementSets)
			throws JavaModelException {
		final Collection ret = new LinkedHashSet(elementSets);
		for (final Iterator it = elementSets.iterator(); it.hasNext();) {
			final Collection set = (Collection) it.next();
			final Collection constNames = new ArrayList();
			for (final Iterator jit = set.iterator(); jit.hasNext();) {
				final IJavaElement elem = (IJavaElement) jit.next();
				if (elem.getElementType() == IJavaElement.FIELD) {
					final IField field = (IField) elem;
					final Object constValue = field.getConstant();
					if (constValue != null)
						constNames.add(field.getElementName());
				}
			}
			if (!distinct(constNames))
				ret.remove(set);
		}
		return ret;
	}

	public static VariableDeclarationStatement getVariableDeclarationStatement(
			ASTNode node) {
		if (node == null)
			return null;
		else if (node instanceof VariableDeclarationStatement)
			return (VariableDeclarationStatement) node;
		else
			return getVariableDeclarationStatement(node.getParent());
	}

	public static boolean inNeedOfTransformation(InfixExpression.Operator op) {
		return op == InfixExpression.Operator.GREATER
				|| op == InfixExpression.Operator.LESS
				|| op == InfixExpression.Operator.GREATER_EQUALS
				|| op == InfixExpression.Operator.LESS_EQUALS;
	}

	public static boolean isConstantField(IField field)
			throws JavaModelException {
		if (field.getConstant() == null)
			return false;
		return true;
	}

	public static boolean isContainedInCaseLabel(ASTNode node) {
		if (node == null)
			return false;
		else if (node.getNodeType() == ASTNode.SWITCH_CASE)
			return true;
		else
			return isContainedInCaseLabel(node.getParent());
	}

	public static boolean isLegalInfixOperator(InfixExpression.Operator op) {
		return op == InfixExpression.Operator.EQUALS
				|| op == InfixExpression.Operator.NOT_EQUALS
				|| op == InfixExpression.Operator.GREATER
				|| op == InfixExpression.Operator.LESS
				|| op == InfixExpression.Operator.GREATER_EQUALS
				|| op == InfixExpression.Operator.LESS_EQUALS;
	}

	public static boolean isSuspiciousAssignmentOperator(Assignment.Operator op) {
		return false;
		// op == Assignment.Operator.BIT_AND_ASSIGN ||
		// op == Assignment.Operator.BIT_OR_ASSIGN ||
		// op == Assignment.Operator.BIT_XOR_ASSIGN;
	}

	public static boolean isSuspiciousInfixOperator(InfixExpression.Operator op) {
		return false;
		// op == InfixExpression.Operator.AND ||
		// op == InfixExpression.Operator.OR ;
		// commented for version 1

		/*
		 * || op == InfixExpression.Operator.GREATER || op ==
		 * InfixExpression.Operator.GREATER_EQUALS || op ==
		 * InfixExpression.Operator.LESS || op ==
		 * InfixExpression.Operator.LESS_EQUALS || op ==
		 * InfixExpression.Operator.XOR;
		 */
	}

	public static String stripQualifiedName(String qualifiedName) {
		if (qualifiedName.indexOf('.') == -1)
			return qualifiedName;

		final int pos = qualifiedName.lastIndexOf('.');
		return qualifiedName.substring(pos + 1);
	}

	public static Collection trimForest(Collection computationForest,
			Collection nonEnumerizableList) {
		final Collection ret = new LinkedHashSet(computationForest);
		final TreeTrimingVisitor visitor = new TreeTrimingVisitor(ret,
				nonEnumerizableList);

		// for each root in the computation forest
		for (final Iterator it = computationForest.iterator(); it.hasNext();) {
			final ComputationNode root = (ComputationNode) it.next();
			root.accept(visitor);
		}
		return ret;
	}

	private static boolean distinct(Collection col) {
		final Comparable[] objs = new Comparable[col.size()];
		col.toArray(objs);
		try {
			Arrays.sort(objs);
		} catch (final ClassCastException E) {
			for (int i = 0; i < objs.length; i++)
				for (int j = i + 1; j < objs.length; j++)
					if (objs[i].equals(objs[j]))
						return false;
			return true;
		}
		for (int i = 1; i < objs.length; i++)
			if (objs[i].equals(objs[i - 1]))
				return false;
		return true;
	}

	private Util() {
	}
}