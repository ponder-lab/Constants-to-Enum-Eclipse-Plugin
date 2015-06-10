package edu.ohio_state.khatchad.refactoring;

import edu.cuny.citytech.refactoring.common.RefactoringPlugin;


public class ConvertConstantsToEnumRefactoringPlugin extends RefactoringPlugin {

	public ConvertConstantsToEnumRefactoringPlugin() {
		plugin = this;
	}

	/* (non-Javadoc)
	 * @see edu.cuny.citytech.refactoring.common.RefactoringPlugin#getRefactoringId()
	 */
	protected String getRefactoringId() {
		return ConvertConstantsToEnumDescriptor.REFACTORING_ID;
	}
}