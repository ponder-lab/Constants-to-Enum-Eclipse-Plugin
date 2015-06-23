/*******************************************************************************
 * Copyright (c) 2009 Benjamin Muskalla and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Benjamin Muskalla - initial API and implementation
 *******************************************************************************/
package edu.ohio_state.khatchad.refactoring.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.ohio_state.khatchad.refactoring.ConvertConstantsToEnumRefactoringPlugin;

public class ConvertConstantsToEnumHandler extends AbstractHandler {

	/**
	 * {@inheritDoc}
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection currentSelection = HandlerUtil
				.getCurrentSelectionChecked(event);

		List selectedFields = getFields(currentSelection);
		try {
			Shell shell = HandlerUtil.getActiveShellChecked(event);
			IField[] fields = (IField[]) selectedFields
					.toArray(new IField[] {});
			ConvertConstantsToEnumWizard
					.startConvertConstantsToEnumRefactoring(fields, shell);
		} catch (final JavaModelException exception) {
			ConvertConstantsToEnumRefactoringPlugin.getDefault().log(exception);
		}

		return null;
	}

	/**
	 * Gets fields from the given selection.
	 */
	private static List getFields(ISelection selection) {
		List fields = new ArrayList();
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Iterator iterator = structuredSelection.iterator();
			while (iterator.hasNext()) {
				Object selectedObject = iterator.next();
				if (selectedObject instanceof IField) {
					fields.add(selectedObject);
				} else if (selectedObject instanceof IType) {
					// need to traverse each of the fields of the selected
					// object.
					IType type = (IType) selectedObject;

					// the fields in the type.
					List fieldsOfType = getFields(type);

					// add them to the list to be returned.
					fields.addAll(fieldsOfType);
				}

				// this condition check if the class compilationUnit get
				// selected, it will convert all possible IFields to Enum
				else if (selectedObject instanceof ICompilationUnit) {
					// need to traverse each of the ITypes.
					ICompilationUnit compilationUnit = (ICompilationUnit) selectedObject;
					List fieldsOfCompilationUnit = getFields(compilationUnit);
					fields.addAll(fieldsOfCompilationUnit);
				} else if (selectedObject instanceof IPackageFragment) {
					// need to traverse each of the package fragments of the
					// selected
					IPackageFragment packageFragment = (IPackageFragment) selectedObject;
					fields.addAll(getFields(packageFragment));
				} else if (selectedObject instanceof IPackageFragmentRoot) {
					IPackageFragmentRoot root = (IPackageFragmentRoot) selectedObject;
					fields.addAll(getFields(root));
				} else if (selectedObject instanceof IJavaProject) {
					IJavaProject iJavaProject = (IJavaProject) selectedObject;
					try {
						IPackageFragmentRoot[] allPackageFragmentRoots = iJavaProject
								.getAllPackageFragmentRoots();
						for (int i = 0; i < allPackageFragmentRoots.length; i++) {
							IPackageFragmentRoot iPackageFragmentRoot = allPackageFragmentRoots[i];
							fields.addAll(getFields(iPackageFragmentRoot));
						}

					} catch (JavaModelException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return fields;
	}

	/**
	 * Gets fields from the given IJavaProject.
	 */
	private static List getFields(IPackageFragmentRoot root) {
		List fields = new ArrayList();
		try {
			IJavaElement[] children = root.getChildren();

			for (int i = 0; i < children.length; i++) {
				IJavaElement iJavaElement = children[i];
				if (iJavaElement instanceof IPackageFragment) {
					IPackageFragment ipackFragment = (IPackageFragment) iJavaElement;
					fields.addAll(getFields(ipackFragment));
				}

			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return fields;
	}

	/**
	 * Gets fields from the given packageFragment.
	 */
	private static List getFields(IPackageFragment packageFragment) {
		List fields = new ArrayList();
		try {
			ICompilationUnit[] compilationUnits = packageFragment
					.getCompilationUnits();

			for (int i = 0; i < compilationUnits.length; i++) {
				ICompilationUnit iCompilationUnit = compilationUnits[i];
				List fieldsOfCompilationUnit = getFields(iCompilationUnit);
				fields.addAll(fieldsOfCompilationUnit);
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return fields;
	}

	/**
	 * Gets fields from the given compilation unit.
	 */
	private static List getFields(ICompilationUnit compilationUnit) {
		List fields = new ArrayList();
		try {
			IType[] typeArray = compilationUnit.getTypes();

			// for each type in typeArray, getFields from that type.
			for (int i = 0; i < typeArray.length; i++) {
				IType type = typeArray[i];
				List fieldsOfType = getFields(type);
				fields.addAll(fieldsOfType);
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}

		return fields;
	}

	/**
	 * Gets fields from the given type.
	 */
	private static List getFields(IType type) {
		List fields = new ArrayList();

		try {
			IField[] fieldsOfType = type.getFields();
			fields.addAll(Arrays.asList(fieldsOfType));
		} catch (JavaModelException e) {
			e.printStackTrace();
		}

		// check for inner classes.
		try {
			IType[] innerTypes = type.getTypes();
			for (int i = 0; i < innerTypes.length; i++) {
				IType innerType = innerTypes[i];
				fields.addAll(getFields(innerType));
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}

		return fields;
	}

}
