package edu.ohio_state.khatchad.refactoring;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.search.FieldDeclarationMatch;
import org.eclipse.jdt.core.search.FieldReferenceMatch;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.LocalVariableDeclarationMatch;
import org.eclipse.jdt.core.search.LocalVariableReferenceMatch;
import org.eclipse.jdt.core.search.MethodDeclarationMatch;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.ui.wizards.NewEnumWizardPage;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

import edu.cuny.citytech.refactoring.common.Refactoring;
import edu.cuny.citytech.refactoring.common.RefactoringPlugin;
import edu.ohio_state.khatchad.refactoring.core.EnumConstantComparator;
import edu.ohio_state.khatchad.refactoring.core.EnumerizationComputer;
import edu.ohio_state.khatchad.refactoring.core.InternalStateStatus;
import edu.ohio_state.khatchad.refactoring.core.Util;

public class ConvertConstantsToEnumRefactoring extends Refactoring {

	static class SearchMatchPurpose {
		public static final SearchMatchPurpose ALTER_INFIX_EXPRESSION = new SearchMatchPurpose();
		public static final SearchMatchPurpose ALTER_NAMESPACE_PREFIX = new SearchMatchPurpose();
		public static final SearchMatchPurpose ALTER_TYPE_DECLARATION = new SearchMatchPurpose();

		private SearchMatchPurpose() {
		}
	}

	void commenceSearch(SearchEngine engine,
			SearchPattern pattern, IJavaSearchScope scope,
			final SearchMatchPurpose purpose,
			IProgressMonitor monitor) throws CoreException {
		engine.search(pattern, new SearchParticipant[] { SearchEngine
				.getDefaultSearchParticipant() }, scope, new SearchRequestor() {

			public void acceptSearchMatch(SearchMatch match)
					throws CoreException {
				if (match.getAccuracy() == SearchMatch.A_ACCURATE
						&& !match.isInsideDocComment())
					matchToPurposeMap.put(match, purpose);
			}
		}, new SubProgressMonitor(monitor, 1,
						SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
	}

	private static EnumConstantDeclaration createNewEnumConstantDeclarataion(
			AST ast, SimpleName constantName, Javadoc docComment,
			Collection annotationCollection) {
		final EnumConstantDeclaration enumConstDecl = ast
				.newEnumConstantDeclaration();
		enumConstDecl.setJavadoc(docComment);
		enumConstDecl.setName(constantName);
		enumConstDecl.modifiers().addAll(annotationCollection);
		return enumConstDecl;
	}

	private static EnumDeclaration createNewEnumDeclaration(AST ast,
			SimpleName name, Collection enumConstantDeclarationCollection,
			Object[] enumTypeModifierCollection) {

		final EnumDeclaration enumDecl = ast.newEnumDeclaration();
		enumDecl.setName(name);
		for (int i = 0; i < enumTypeModifierCollection.length; i++)
			enumDecl.modifiers().add(enumTypeModifierCollection[i]);
		enumDecl.enumConstants().addAll(enumConstantDeclarationCollection);
		return enumDecl;
	}

	private static Type getNewType(AST ast, String typeName) {
		final SimpleName name = ast.newSimpleName(typeName);
		return ast.newSimpleType(name);
	}

	EnumerizationComputer computer;

	/**
	 * The input fields to attempt to refactor.
	 */
	List fieldsToRefactor = new LinkedList();

	/**
	 * A map from search matches to the reason they were searched for. 
	 * The key set is the declarations that need to be transformed.
	 */
	final Map matchToPurposeMap = new LinkedHashMap();

	private final Map packageNames = new LinkedHashMap();

	private final Map removedFieldNodes = new LinkedHashMap();

	private final Map simpleTypeNames = new LinkedHashMap();

	private String simpleTypeName;
	
	private String packageName;

	protected final Map changes = new LinkedHashMap();

	/**
	 * Default ctor.
	 */
	public ConvertConstantsToEnumRefactoring() {
	}

	public ConvertConstantsToEnumRefactoring(List fieldsToRefactor) {
		this.fieldsToRefactor = new LinkedList(fieldsToRefactor);
	}

	/**
	 * @param fieldsToRefactor
	 *            the fieldsToRefactor to set
	 */
	public void setFieldsToRefactor(List fieldsToRefactor) {
		this.fieldsToRefactor = fieldsToRefactor;
	}

	public Collection getFieldsToRefactor() {
		return this.fieldsToRefactor;
	}

	private Collection extractConstants(Collection col) {
		final Collection ret = new LinkedHashSet();

		for (final Iterator it = col.iterator(); it.hasNext();) {
			final IJavaElement elem = (IJavaElement) it.next();
			if (elem.getElementType() == IJavaElement.FIELD)
				ret.add(elem);
		}

		ret.retainAll(this.fieldsToRefactor);
		return ret;
	}

	private String getFullyQualifiedName(IJavaElement elem) {
		// Get the associated set.
		Collection set = null;
		for (final Iterator it = this.computer.getEnumerizationForest()
				.iterator(); it.hasNext();) {
			final Collection col = (Collection) it.next();
			if (col.contains(elem))
				set = col;
		}

		// Get the fully qualified type name.
		final StringBuffer fqn = new StringBuffer(this.simpleTypeNames.get(set)
				.toString());
		fqn.insert(0, '.');
		fqn.insert(0, this.packageNames.get(set));
		return fqn.toString();
	}

	/**
	 * @return
	 */
	IJavaProject getJavaProject() {
		/*
		 * TODO: Just a simulation
		 */
		return ((IField) this.fieldsToRefactor.iterator().next())
				.getJavaProject();
	}

	/**
	 * @param monitor
	 * @return
	 * @throws JavaModelException
	 */
	private IPackageFragment getPackageFragment(String packageName,
			IProgressMonitor monitor) throws JavaModelException {
		final IPackageFragmentRoot root = this.getPackageFragmentRoot();
		return root.createPackageFragment(packageName, false, monitor);
	}

	/**
	 * @return
	 */
	private IPackageFragmentRoot getPackageFragmentRoot()
			throws JavaModelException {
		/*
		 * TODO: Just a simulation
		 */
		final IJavaProject project = this.getJavaProject();
		return project.getPackageFragmentRoots()[0];
	}

	/*
	 * TODO: This function creates a new enum type in its own file. However, the
	 * user may want to insert the new enum type as an inner type in some
	 * existing class. Such a method should create a new enum type as an ASTNode
	 * attached to the AST of the existing class, as opposed to using the
	 * "Create New Enum Type" plug-in programmatically (which is what this
	 * method does).
	 */
	RefactoringStatus insertNewEnumType(IProgressMonitor monitor)
			throws CoreException {
		final RefactoringStatus status = new RefactoringStatus();
		final AST ast = AST.newAST(AST.JLS8);
		for (final Iterator it = this.computer.getEnumerizationForest()
				.iterator(); it.hasNext();) {
			final Collection col = (Collection) it.next();

			if (col.isEmpty())
				continue;

			final Collection constants = this.extractConstants(col);
			final Map newEnumConstantToOldConstantFieldMap = new HashMap();
			final EnumConstantComparator comparator = new EnumConstantComparator(
					newEnumConstantToOldConstantFieldMap);
			final SortedSet enumConstantDeclarationCollection = new TreeSet(
					comparator);

			final Map annotationToQualifiedNameMap = new HashMap();

			for (final Iterator cit = constants.iterator(); cit.hasNext();) {
				final IField constantField = (IField) cit.next();
				final FieldDeclaration originalFieldDeclaration = (FieldDeclaration) this.removedFieldNodes
						.get(constantField);

				// Get annotations.
				final Collection annotationCollection = new LinkedHashSet();
				for (final Iterator mit = originalFieldDeclaration.modifiers()
						.iterator(); mit.hasNext();) {
					final Object o = mit.next();
					if (o instanceof Annotation) {
						final Annotation oldAnnotation = (Annotation) o;
						final Annotation newAnnotation = (Annotation) ASTNode
								.copySubtree(ast, oldAnnotation);
						annotationToQualifiedNameMap.put(newAnnotation,
								oldAnnotation.resolveTypeBinding()
										.getQualifiedName());
						annotationCollection.add(newAnnotation);
					}
				}

				// Get the javadoc.
				final Javadoc originalJavadoc = originalFieldDeclaration
						.getJavadoc();
				final Javadoc newJavadoc = (Javadoc) ASTNode.copySubtree(ast,
						originalJavadoc);

				final EnumConstantDeclaration constDecl = createNewEnumConstantDeclarataion(
						ast, ast.newSimpleName(constantField.getElementName()),
						newJavadoc, annotationCollection);

				newEnumConstantToOldConstantFieldMap.put(constDecl, constantField);
				enumConstantDeclarationCollection.add(constDecl);
			}

			// Get the consistent access modifier of the enum constants.
			final int flag = Util.getConsistentVisibility(constants);
			/*******************************************************************
			 * * TODO: Need condition check here: 1. If enum is in its own file
			 * then only public and package default are allowed. 2. Else, if
			 * enum is embedded in another type, then all visibilities are
			 * allowed, but may need more checking here for private!.
			 ******************************************************************/
			if (!(Flags.isPublic(flag) || Flags.isPackageDefault(flag)))
				status
						.addFatalError(Messages.ConvertConstantsToEnumRefactoring_EnumTypeMustHaveCorrectVisibility);

			EnumDeclaration newEnumDeclaration = null;
			// only add modifier if it is not package default.
			if (!Flags.isPackageDefault(flag)) {
				final Modifier newModifier = ast
						.newModifier(Modifier.ModifierKeyword
								.fromFlagValue(flag));

				newEnumDeclaration = createNewEnumDeclaration(ast, ast
						.newSimpleName((String) this.simpleTypeNames.get(col)),
						enumConstantDeclarationCollection,
						new Object[] { newModifier });
			} else
				newEnumDeclaration = createNewEnumDeclaration(ast, ast
						.newSimpleName((String) this.simpleTypeNames.get(col)),
						enumConstantDeclarationCollection, new Object[] {});

			// TODO [bm] pretty dirty hack to workaround 16: Refactoring should not use UI components for code changes
			//			 http://code.google.com/p/constants-to-enum-eclipse-plugin/issues/detail?id=16
			final NewEnumWizardPage[] result = new NewEnumWizardPage[1];
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					result[0]= new NewEnumWizardPage();
				}
			});
			NewEnumWizardPage page = result[0];
			page.setTypeName((String) this.simpleTypeNames.get(col), false);

			final IPackageFragmentRoot root = this.getPackageFragmentRoot();
			page.setPackageFragmentRoot(root, false);

			final IPackageFragment pack = this.getPackageFragment(
					(String) this.packageNames.get(col), monitor);
			page.setPackageFragment(pack, false);

			/*******************************************************************
			 * * TODO: This is somewhat of a dirty workaround. Basically, I am
			 * creating a new Enum type only to replace the root AST node. If
			 * you have any better ideas please let me know!
			 ******************************************************************/
			/*******************************************************************
			 * TODO: Need a way of inserting this new type such that it appears
			 * in the text changes for rollback purposes, etc.
			 ******************************************************************/
			try {
				page.createType(monitor);
			} catch (final InterruptedException E) {
				status.addFatalError(E.getMessage());
			}

			// Modify the newly created enum type.
			final IType newEnumType = page.getCreatedType();
			final CompilationUnit node = (CompilationUnit) Util.getASTNode(
					newEnumType, monitor);

			final ASTRewrite astRewrite = ASTRewrite.create(node.getAST());
			final ImportRewrite importRewrite = ImportRewrite
					.create(node, true);

			final EnumDeclaration oldEnumDeclaration = (EnumDeclaration) node
					.types().get(0);

			// Add imports for annotations to the enum constants.
			for (final Iterator eit = newEnumDeclaration.enumConstants()
					.iterator(); eit.hasNext();) {
				final Object obj = eit.next();
				final EnumConstantDeclaration ecd = (EnumConstantDeclaration) obj;
				for (final Iterator emit = ecd.modifiers().iterator(); emit
						.hasNext();) {
					final Object o = emit.next();
					if (o instanceof Annotation) {
						final Annotation anno = (Annotation) o;
						final String newName = importRewrite
								.addImport((String) annotationToQualifiedNameMap
										.get(anno));
						anno.setTypeName(ast.newName(newName));
					}
				}
			}
			/*
			 * TODO: Need to remove resulting unused imports, but I am unsure of
			 * how to do that.
			 */

			astRewrite.replace(oldEnumDeclaration, newEnumDeclaration, null);
			this.rewriteAST(newEnumType.getCompilationUnit(), astRewrite,
					importRewrite);
		}

		return status;
	}

	private boolean isEmptyEdit(TextEdit edit) {
		return edit.getClass() == MultiTextEdit.class && !edit.hasChildren();
	}

	private RefactoringStatus removeConstField(ASTRewrite astRewrite,
			ImportRewrite importRewrite,
			FieldDeclaration constFieldDeclaration, IField constField) {
		final RefactoringStatus status = new RefactoringStatus();
		astRewrite.remove(constFieldDeclaration, null);
		this.removedFieldNodes.put(constField, constFieldDeclaration);
		return status;
	}

	/**
	 * @return
	 */
	RefactoringStatus reportNonEnumerizableInputConstants() {
		final RefactoringStatus ret = new RefactoringStatus();
		final Collection enumerizableElements = Util
				.flattenForest(this.computer.getEnumerizationForest());
		for (final Iterator it = this.fieldsToRefactor.iterator(); it.hasNext();) {
			final IField field = (IField) it.next();
			if (!enumerizableElements.contains(field)) {
				String message = Messages.ConvertConstantsToEnumRefactoring_RefactoringNotPossible;
				ret.addWarning(MessageFormat.format(message, new Object[] {field.getElementName()}));
			}
		}
		return ret;
	}

	private void retrievePackageNames() {
		/** * TODO: Get real package names. ** */
//		int counter = 0;
		for (final Iterator it = this.computer.getEnumerizationForest()
				.iterator(); it.hasNext();) {
			final Collection col = (Collection) it.next();
//			this.packageNames.put(col, this.packageName + counter++); //$NON-NLS-1$
			this.packageNames.put(col, this.packageName);
		}
	}

	private void retrieveSimpleTypeNames() {
		/** * TODO: Get real type names. ** */
//		int counter = 0;
		for (final Iterator it = this.computer.getEnumerizationForest()
				.iterator(); it.hasNext();) {
			final Collection col = (Collection) it.next();
//			this.simpleTypeNames.put(col, simpleTypeName + counter++);
			this.simpleTypeNames.put(col, simpleTypeName);
		}
	}

	public String getSimpleTypeName() {
		return simpleTypeName;
	}

	public void setSimpleTypeName(String simpleTypeName) {
		this.simpleTypeName = simpleTypeName;
	}

	void retrieveTypeNames() {
		this.retrieveSimpleTypeNames();
		this.retrievePackageNames();
	}

	private void rewriteAST(ICompilationUnit unit, ASTRewrite astRewrite,
			ImportRewrite importRewrite) {
		try {
			final MultiTextEdit edit = new MultiTextEdit();
			final TextEdit astEdit = astRewrite.rewriteAST();

			if (!this.isEmptyEdit(astEdit))
				edit.addChild(astEdit);
			final TextEdit importEdit = importRewrite
					.rewriteImports(new NullProgressMonitor());
			if (!this.isEmptyEdit(importEdit))
				edit.addChild(importEdit);
			if (this.isEmptyEdit(edit))
				return;

			TextFileChange change = (TextFileChange) this.changes.get(unit);
			if (change == null) {
				change = new TextFileChange(unit.getElementName(), (IFile) unit
						.getResource());
				change.setTextType("java"); //$NON-NLS-1$
				change.setEdit(edit);
			} else
				change.getEdit().addChild(edit);

			this.changes.put(unit, change);
		} catch (final MalformedTreeException exception) {
			RefactoringPlugin.getDefault().log(exception);
		} catch (final IllegalArgumentException exception) {
			RefactoringPlugin.getDefault().log(exception);
		} catch (final CoreException exception) {
			RefactoringPlugin.getDefault().log(exception);
		}
	}

	private void rewriteDeclarationsAndNamespaces(CompilationUnit node,
			SearchMatch match, RefactoringStatus status, ASTRewrite astRewrite,
			ImportRewrite importRewrite) throws CoreException {
		// final ASTNode result = NodeFinder.perform(node, match
		// .getOffset(), match.getLength());
		final ASTNode result = Util.getExactASTNode(node, match);

		// Must be simple name node.
		if (result.getNodeType() != ASTNode.SIMPLE_NAME) {
			final String errorMessage = Messages.ConvertConstantsToEnumRefactoring_WrongType;
			status
					.merge(RefactoringStatus
							.createFatalErrorStatus(MessageFormat.format(errorMessage,new Object[] {node, node.getClass()})));
			final IStatus stateStatus = new InternalStateStatus(IStatus.ERROR,
					errorMessage);
			throw new CoreException(stateStatus);
		}

		// Get the fully qualified type name.
		final String fqn = this.getFullyQualifiedName(((Name) result)
				.resolveBinding().getJavaElement());

		if (match instanceof FieldDeclarationMatch
				&& this.fieldsToRefactor.contains(match.getElement()))
			status.merge(this.removeConstField(astRewrite, importRewrite, Util
					.getFieldDeclaration(result), (IField) match.getElement()));

		else if (match instanceof FieldDeclarationMatch)
			status.merge(this.rewriteFieldDeclaration(astRewrite,
					importRewrite, Util.getFieldDeclaration(result), fqn));

		// Workaround for Bug 207257.
		else if (match instanceof LocalVariableDeclarationMatch
				|| match instanceof LocalVariableReferenceMatch)
			if (((IVariableBinding) ((Name) result).resolveBinding())
					.isParameter())
				status.merge(this.rewriteFormalParameterDeclaration(astRewrite,
						importRewrite, Util
								.getSingleVariableDeclaration(result), fqn));

			else
				status.merge(this.rewriteLocalVariableDeclaration(astRewrite,
						importRewrite, Util
								.getVariableDeclarationStatement(result), fqn));

		else if (match instanceof MethodDeclarationMatch)
			status.merge(this.rewriteMethodDeclaration(astRewrite,
					importRewrite, Util.getMethodDeclaration(result), fqn));

		else if (match instanceof FieldReferenceMatch)
			// Rewrite the reference.
			status.merge(this.rewriteReference(astRewrite, importRewrite,
					(SimpleName) result, fqn));

	}

	private void rewriteExpressions(CompilationUnit node, SearchMatch match,
			RefactoringStatus status, ASTRewrite astRewrite,
			ImportRewrite importRewrite) {
		final ASTNode result = NodeFinder.perform(node, match.getOffset(),
				match.getLength());
		final InfixExpression ie = Util.getInfixExpression(result);
		if (ie == null) // there is none.
			return;
		else if (Util.inNeedOfTransformation(ie.getOperator())) {
			// Get the fully qualified type name.
			final String fqn = this.getFullyQualifiedName(((Name) result)
					.resolveBinding().getJavaElement());
			this.rewriteInfixExpression(astRewrite, importRewrite, ie, fqn);
		}
	}

	private RefactoringStatus rewriteFieldDeclaration(ASTRewrite astRewrite,
			ImportRewrite importRewrite, FieldDeclaration oldFieldDeclaration,
			String fullyQualifiedTypeName) {
		final RefactoringStatus status = new RefactoringStatus();
		final AST ast = oldFieldDeclaration.getAST();
		final String typeName = importRewrite.addImport(fullyQualifiedTypeName);
		final Type newType = getNewType(ast, typeName);
		final Type oldType = oldFieldDeclaration.getType();
		astRewrite.replace(oldType, newType, null);
		return status;
	}

	private RefactoringStatus rewriteFormalParameterDeclaration(
			ASTRewrite astRewrite, ImportRewrite importRewrite,
			SingleVariableDeclaration oldFormalParameterDeclaration,
			String fullyQualifiedTypeName) {

		final RefactoringStatus status = new RefactoringStatus();
		final String typeName = importRewrite.addImport(fullyQualifiedTypeName);
		final AST ast = oldFormalParameterDeclaration.getAST();
		final Type newType = getNewType(ast, typeName);
		final Type oldType = oldFormalParameterDeclaration.getType();
		astRewrite.replace(oldType, newType, null);
		return status;
	}

	private RefactoringStatus rewriteInfixExpression(ASTRewrite astRewrite,
			ImportRewrite importRewrite, InfixExpression ie,
			String fullyQualifiedTypeName) {
		final RefactoringStatus status = new RefactoringStatus();
		final AST ast = ie.getAST();

		final Expression leftExpCopy = (Expression) ASTNode.copySubtree(ast, ie
				.getLeftOperand());
		final Expression rightExpCopy = (Expression) ASTNode.copySubtree(ast,
				ie.getRightOperand());

		final NumberLiteral zero = ast.newNumberLiteral();
		astRewrite.replace(ie.getRightOperand(), zero, null);

		final MethodInvocation newInvocation = ast.newMethodInvocation();
		newInvocation.setExpression(leftExpCopy);
		newInvocation.setName(ast.newSimpleName("compareTo")); //$NON-NLS-1$
		newInvocation.arguments().add(rightExpCopy);

		astRewrite.replace(ie.getLeftOperand(), newInvocation, null);

		if (((ASTNode) newInvocation.arguments().get(0)).getNodeType() == ASTNode.SIMPLE_NAME
				&& this.fieldsToRefactor.contains(((SimpleName) ie
						.getRightOperand()).resolveBinding().getJavaElement()))
			this.rewriteReference(astRewrite, importRewrite,
					(SimpleName) newInvocation.arguments().get(0),
					fullyQualifiedTypeName);

		if (((ASTNode) newInvocation.getExpression()).getNodeType() == ASTNode.SIMPLE_NAME
				&& this.fieldsToRefactor.contains(((SimpleName) ie
						.getLeftOperand()).resolveBinding().getJavaElement()))
			this.rewriteReference(astRewrite, importRewrite,
					(SimpleName) newInvocation.getExpression(),
					fullyQualifiedTypeName);

		return status;
	}

	private RefactoringStatus rewriteLocalVariableDeclaration(
			ASTRewrite astRewrite, ImportRewrite importRewrite,
			VariableDeclarationStatement oldVariableDeclaration,
			String fullyQualifiedTypeName) {

		final RefactoringStatus status = new RefactoringStatus();
		final AST ast = oldVariableDeclaration.getAST();
		final String typeName = importRewrite.addImport(fullyQualifiedTypeName);
		final Type newType = getNewType(ast, typeName);
		final Type oldType = oldVariableDeclaration.getType();
		astRewrite.replace(oldType, newType, null);
		return status;
	}

	private RefactoringStatus rewriteMethodDeclaration(ASTRewrite astRewrite,
			ImportRewrite importRewrite,
			MethodDeclaration oldMethodDeclaration,
			String fullyQualifiedTypeName) {

		final RefactoringStatus status = new RefactoringStatus();
		final AST ast = oldMethodDeclaration.getAST();
		final String typeName = importRewrite.addImport(fullyQualifiedTypeName);
		final Type newType = getNewType(ast, typeName);
		final Type oldType = oldMethodDeclaration.getReturnType2();
		astRewrite.replace(oldType, newType, null);
		return status;
	}

	private RefactoringStatus rewriteReference(ASTRewrite astRewrite,
			ImportRewrite importRewrite, SimpleName name,
			String fullyQualifiedTypeName) {
		final RefactoringStatus status = new RefactoringStatus();

		// get the node to replace.
		final Name nodeToReplace = Util.getTopmostName(name);

		// If its in a case statement.
		if (Util.isContainedInCaseLabel(name)) {
			if (!nodeToReplace.isSimpleName()) // Need to remove prefix.
				astRewrite.replace(nodeToReplace, name, null);
			return status;
		}

		// Make a copy of the simple name.
		final AST ast = name.getAST();
		final SimpleName nameCopy = (SimpleName) ASTNode.copySubtree(ast, name);

		final String typeName = importRewrite.addImport(fullyQualifiedTypeName);
		final QualifiedName newNameNode = ast.newQualifiedName(ast
				.newName(typeName), nameCopy);

		astRewrite.replace(nodeToReplace, newNameNode, null);
		return status;
	}

	protected void rewriteCompilationUnit(ICompilationUnit unit,
			Collection matches, CompilationUnit node, RefactoringStatus status,
			IProgressMonitor monitor) throws CoreException {
		final ASTRewrite astRewrite = ASTRewrite.create(node.getAST());
		final ImportRewrite importRewrite = ImportRewrite.create(node, true);

		for (final Iterator it = matches.iterator(); it.hasNext();) {
			final SearchMatch match = (SearchMatch) it.next();
			if (match.getAccuracy() == SearchMatch.A_ACCURATE
					&& !match.isInsideDocComment())
				if (this.matchToPurposeMap.get(match) == SearchMatchPurpose.ALTER_TYPE_DECLARATION
						|| this.matchToPurposeMap.get(match) == SearchMatchPurpose.ALTER_NAMESPACE_PREFIX)
					this.rewriteDeclarationsAndNamespaces(node, match, status,
							astRewrite, importRewrite);
				else if (this.matchToPurposeMap.get(match) == SearchMatchPurpose.ALTER_INFIX_EXPRESSION)
					this.rewriteExpressions(node, match, status, astRewrite,
							importRewrite);
		}

		this.rewriteAST(unit, astRewrite, importRewrite);
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getPackageName() {
		return packageName;
	}

	public RefactoringStatus checkFinalConditions(final IProgressMonitor monitor) throws CoreException,
			OperationCanceledException {
				final RefactoringStatus status = new RefactoringStatus();
				try {
					monitor.beginTask(Messages.ConvertConstantsToEnumRefactoring_CheckingPreconditions, 2);
			
					final IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
					this.computer = new EnumerizationComputer(this.fieldsToRefactor,
							scope, monitor);
			
					// build the enumerization forest.
					/*
					 * TODO: Will treat this as a 'blackbox' for now. For more details
					 * on the internals of this method, check the paper available at:
					 * http://www.cse.ohio-state.edu/~khatchad/papers/khatchad-TR26.pdf
					 * or a shorter version at
					 * http://presto.cse.ohio-state.edu/pubs/icsm07.pdf.
					 */
					this.computer.compute();
			
					/*
					 * TODO: The enumerization forest build by the enum computer
					 * consists of only the *minimal* sets which can be transformed into
					 * an enum type. That is, elements are grouped together in each set
					 * based *only* upon their type dependencies. Therefore, it is
					 * possible to, if desired, to further union these sets to build
					 * large (in terms of members) types. In the initial test case, the
					 * field DECREASE_SPEED is an example of this problem. Since
					 * DECREASE_SPEED does not currently share type dependencies with
					 * any of the other automobile actions, the computer places it in a
					 * singleton set. However, it is clear that DECREASE_SPEED should
					 * belong to the set consisting of the other automobile actions. As
					 * such, we may want a sophisticated UI that presents the input
					 * constants in sets produced by the enumerization computer, then
					 * allow the user to further union the sets as desired. After the
					 * user has manipulated the sets, we would only need to run the new
					 * "forest" through the member constraint filter which is a public
					 * static method of the EnumerizationComputer.
					 */
			
					// check to see if any of the input constants weren't enumerizable.
					final RefactoringStatus nonEnumStatus = this
							.reportNonEnumerizableInputConstants();
					status.merge(nonEnumStatus);
			
					// Get names for the new types.
					this.retrieveTypeNames();
					
					for (final Iterator fit = this.computer.getEnumerizationForest()
							.iterator(); fit.hasNext();) {
						final Collection col = (Collection) fit.next();
						for (final Iterator cit = col.iterator(); cit.hasNext();) {
							final IJavaElement elem = (IJavaElement) cit.next();
			
							// The search engine.
							final SearchEngine engine = new SearchEngine();
			
							// The search pattern corresponding to the entities whose
							// type must be altered.
							SearchPattern pattern = SearchPattern.createPattern(elem,
									IJavaSearchConstants.DECLARATIONS,
									SearchPattern.R_EXACT_MATCH);
			
							// Search for declarations (must always do this since each
							// element's type must be altered).
							commenceSearch(engine, pattern, scope,
									SearchMatchPurpose.ALTER_TYPE_DECLARATION, monitor);
			
							// if the current element is a that of an original input
							// constant ...
							if (this.fieldsToRefactor.contains(elem)) {
								// The search pattern corresponding to the references to
								// the constant whose parent expression(s) must be
								// altered (more like tweaked).
								pattern = SearchPattern.createPattern(elem,
										IJavaSearchConstants.REFERENCES,
										SearchPattern.R_EXACT_MATCH);
			
								commenceSearch(engine, pattern, scope,
										SearchMatchPurpose.ALTER_NAMESPACE_PREFIX,
										monitor);
							}
			
							// if the current element needs infix expression
							// manipulation ...
							if (this.computer
									.getElemToLegalInfixExpressionSourceRangeMap()
									.containsKey(elem)) {
								pattern = SearchPattern.createPattern(elem,
										IJavaSearchConstants.REFERENCES,
										SearchPattern.R_EXACT_MATCH);
			
								commenceSearch(engine, pattern, scope,
										SearchMatchPurpose.ALTER_INFIX_EXPRESSION,
										monitor);
							}
						}
					}
			
					// The compilation units needing to be altered mapped to the
					// appropriate search matches.
					final Map units = new HashMap();
					for (final Iterator it = this.matchToPurposeMap.keySet().iterator(); it
							.hasNext();) {
						final SearchMatch match = (SearchMatch) it.next();
						final IJavaElement element = (IJavaElement) match.getElement();
						final ICompilationUnit unit = Util.getIMember(element)
								.getCompilationUnit();
						if (unit != null) {
							Collection searchMatchCollection = (Collection) units
									.get(unit);
							if (searchMatchCollection == null) {
								searchMatchCollection = new ArrayList();
								units.put(unit, searchMatchCollection);
							}
							searchMatchCollection.add(match);
						}
					}
			
					final Map projects = new HashMap();
					for (final Iterator it = units.keySet().iterator(); it.hasNext();) {
						final ICompilationUnit unit = (ICompilationUnit) it.next();
						final IJavaProject project = unit.getJavaProject();
						if (project != null) {
							Collection unitsCollection = (Collection) projects
									.get(project);
							if (unitsCollection == null) {
								unitsCollection = new ArrayList();
								projects.put(project, unitsCollection);
							}
							unitsCollection.add(unit);
						}
					}
			
					final ASTRequestor requestor = new ASTRequestor() {
			
						public void acceptAST(ICompilationUnit source,
								CompilationUnit ast) {
							try {
								ConvertConstantsToEnumRefactoring.this
										.rewriteCompilationUnit(source,
												(Collection) units.get(source), ast,
												status, monitor);
							} catch (CoreException exception) {
								RefactoringPlugin.getDefault().log(exception);
							}
						}
					};
			
					final IProgressMonitor subMonitor = new SubProgressMonitor(monitor,
							1);
					try {
						final Set set = projects.keySet();
						subMonitor.beginTask(Messages.ConvertConstantsToEnumRefactoring_CompilingSource, set.size());
			
						for (final Iterator it = set.iterator(); it.hasNext();) {
							final IJavaProject project = (IJavaProject) it.next();
							final ASTParser parser = ASTParser.newParser(AST.JLS8);
							parser.setProject(project);
							parser.setResolveBindings(true);
							final Collection collection = (Collection) projects
									.get(project);
							parser.createASTs((ICompilationUnit[]) collection
									.toArray(new ICompilationUnit[collection.size()]),
									new String[0], requestor, new SubProgressMonitor(
											subMonitor, 1));
						}
			
					} finally {
						subMonitor.done();
					}
				} finally {
					monitor.done();
				}
			
				status.merge(this.insertNewEnumType(monitor));
				return status;
			}

	public RefactoringStatus checkInitialConditions(IProgressMonitor monitor) throws CoreException,
			OperationCanceledException {
				final RefactoringStatus status = new RefactoringStatus();
				try {
					monitor.beginTask(Messages.ConvertConstantsToEnumRefactoring_CheckingPreconditions, 1);
					if (this.fieldsToRefactor.isEmpty())
						status
								.merge(RefactoringStatus
										.createFatalErrorStatus(Messages.ConvertConstantsToEnumRefactoring_FieldsHaveNotBeenSpecified));
			
					else {
						for (final Iterator it = this.fieldsToRefactor.listIterator(); it
								.hasNext();) {
							final IField field = (IField) it.next();
							if (!field.exists()) {
								String message = Messages.ConvertConstantsToEnumRefactoring_FileDoesNotExist;
								status.addWarning(MessageFormat.format(message, new Object[] {field.getElementName()}));
								it.remove();
							}
			
							else if (!field.isBinary()
									&& !field.getCompilationUnit().isStructureKnown()) {
								String message = Messages.ConvertConstantsToEnumRefactoring_CUContainsCompileErrors;
								status.addWarning(MessageFormat.format(message, new Object[] {field.getCompilationUnit().getElementName()}));
								it.remove();
							}
			
							else if (field.getElementName().equals("serialVersionUID")) { //$NON-NLS-1$
								String message = Messages.ConvertConstantsToEnumRefactoring_FieldNotEligibleForEnum;
								status.addWarning(MessageFormat.format(message, new Object[] {field.getElementName()}));
								it.remove();
							}
			
							else if (Signature.getTypeSignatureKind(field
									.getTypeSignature()) != Signature.BASE_TYPE_SIGNATURE) {
								String message = Messages.ConvertConstantsToEnumRefactoring_FieldMustBePrimitive;
								status.addWarning(MessageFormat.format(message, new Object[] {field.getElementName()}));
								it.remove();
							}
			
							else if (!Util.isConstantField(field)) {
								String message = Messages.ConvertConstantsToEnumRefactoring_FieldIsNotAConstant;
								status.addWarning(MessageFormat.format(message, new Object[] {field.getElementName()}));
								it.remove();
							}
			
							else if (Flags.isVolatile(field.getFlags())
									|| Flags.isTransient(field.getFlags())) {
								String message = Messages.ConvertConstantsToEnumRefactoring_FieldCannotBeExpressedAsEnum;
								status.addWarning(MessageFormat.format(message, new Object[] {field.getElementName()}));
								it.remove();
							}
			
							if (Signature.getElementType(field.getTypeSignature()) == Signature.SIG_BOOLEAN) {
								String message = Messages.ConvertConstantsToEnumRefactoring_FieldIsBoolean;
								status
										.addWarning(message);
								status.addWarning(MessageFormat.format(message, new Object[] {field.getElementName()}));
								it.remove();
							}
						}
						if (this.fieldsToRefactor.isEmpty())
							status
									.addFatalError(Messages.ConvertConstantsToEnumRefactoring_PreconditionFailed);
					}
			
				} finally {
					monitor.done();
				}
				return status;
			}

	public Change createChange(IProgressMonitor monitor) throws CoreException,
			OperationCanceledException {
				try {
					monitor.beginTask(Messages.ConvertConstantsToEnumRefactoring_CreatingChange, 1);
					final Collection changes = this.changes.values();
					final CompositeChange change = new CompositeChange(this.getName(),
							(Change[]) changes.toArray(new Change[changes.size()])) {
						public ChangeDescriptor getDescriptor() {
							String project = ConvertConstantsToEnumRefactoring.this
									.getJavaProject().getElementName();
							String description = Messages.ConvertConstantsToEnum_Name;
							Map arguments = new HashMap();
							return new RefactoringChangeDescriptor(
									new ConvertConstantsToEnumDescriptor(project,
											description, new String(), arguments));
						}
					};
					return change;
				} finally {
					monitor.done();
				}
			}

	public String getName() {
		return Messages.ConvertConstantsToEnum_Name;
	}

	public RefactoringStatus initialize(Map arguments) {
		return new RefactoringStatus();
	}
}