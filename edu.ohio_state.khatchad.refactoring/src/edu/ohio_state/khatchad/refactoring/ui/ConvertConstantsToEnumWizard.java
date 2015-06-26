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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.jdt.internal.ui.refactoring.actions.RefactoringStarter;
import org.eclipse.jdt.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.swt.widgets.Shell;

import edu.cuny.citytech.refactoring.common.core.Refactoring;
import edu.ohio_state.khatchad.refactoring.ConvertConstantsToEnumRefactoring;
import edu.ohio_state.khatchad.refactoring.Messages;

/**
 * Refactoring wizard for the Convert Constants to Enum refactoring.
 */
public class ConvertConstantsToEnumWizard extends RefactoringWizard {

	private static final String TYPE_PAGE_NAME = "convert.to.enum.type.page"; //$NON-NLS-1$

	public ConvertConstantsToEnumWizard(
			Refactoring refactoring, String pageTitle) {
		super(refactoring, WIZARD_BASED_USER_INTERFACE
				| PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle(pageTitle);
		setDefaultPageImageDescriptor(RefactoringPluginImages.DESC_WIZBAN_REFACTOR_CONVERT_CONSTANTS_ENUM);
	}

	protected void addUserInputPages() {
		addPage(new ConvertToEnumTypePage(TYPE_PAGE_NAME));
	}

	public boolean isHelpAvailable() {
		return false;
	}
	
	public static void startConvertConstantsToEnumRefactoring(
			final IField[] fields, final Shell shell) throws JavaModelException {
		List fieldsToRefactor = Arrays.asList(fields);
		Refactoring convertConstantsToEnumRefactoring = new ConvertConstantsToEnumRefactoring(
				fieldsToRefactor);
		ConvertConstantsToEnumWizard wizard = new ConvertConstantsToEnumWizard(
				convertConstantsToEnumRefactoring,
				Messages.ConvertConstantsToEnum_Name);
		new RefactoringStarter().activate(wizard, shell,
				RefactoringMessages.OpenRefactoringWizardAction_refactoring,
				RefactoringSaveHelper.SAVE_REFACTORING);
	}
}
