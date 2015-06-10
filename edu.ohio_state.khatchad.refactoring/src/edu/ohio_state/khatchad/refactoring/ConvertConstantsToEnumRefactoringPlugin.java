package edu.ohio_state.khatchad.refactoring;


public class ConvertConstantsToEnumRefactoringPlugin extends RefactoringPlugin {

	public ConvertConstantsToEnumRefactoringPlugin() {
		plugin = this;
	}

	/* (non-Javadoc)
	 * @see edu.ohio_state.khatchad.refactoring.RefactoringPlugin#getRefactoringId()
	 */
	protected String getRefactoringId() {
		return ConvertConstantsToEnumDescriptor.REFACTORING_ID;
	}
}