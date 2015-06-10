package edu.ohio_state.khatchad.refactoring;

import java.util.Map;

public class ConvertConstantsToEnumRefactoringContribution extends
		edu.ohio_state.khatchad.refactoring.RefactoringContribution {

	public org.eclipse.ltk.core.refactoring.RefactoringDescriptor createDescriptor(
			String id, String project, String description, String comment,
			Map arguments, int flags) {
		return new ConvertConstantsToEnumDescriptor(project, description,
				comment, arguments);
	}
}