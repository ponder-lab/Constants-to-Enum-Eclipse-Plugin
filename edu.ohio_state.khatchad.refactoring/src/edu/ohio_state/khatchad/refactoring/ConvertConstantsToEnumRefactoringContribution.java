package edu.ohio_state.khatchad.refactoring;

import java.util.Map;

import edu.cuny.citytech.refactoring.common.RefactoringContribution;

public class ConvertConstantsToEnumRefactoringContribution extends
		RefactoringContribution {

	public org.eclipse.ltk.core.refactoring.RefactoringDescriptor createDescriptor(
			String id, String project, String description, String comment,
			Map arguments, int flags) {
		return new ConvertConstantsToEnumDescriptor(project, description,
				comment, arguments);
	}
}