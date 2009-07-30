/**
 * 
 */
package edu.ohio_state.khatchad.refactoring;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 * @author raffi
 *
 */
public class ConvertConstantsToEnumWizard extends RefactoringWizard {

	public ConvertConstantsToEnumWizard(ConvertConstantsToEnumRefactoring refactoring, String pageTitle) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle(pageTitle);
	}

	protected void addUserInputPages() {
	}
}
