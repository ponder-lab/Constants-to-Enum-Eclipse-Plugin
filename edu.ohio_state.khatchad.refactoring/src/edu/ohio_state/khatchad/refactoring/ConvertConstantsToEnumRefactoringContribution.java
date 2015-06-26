package edu.ohio_state.khatchad.refactoring;

import java.util.Map;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import edu.cuny.citytech.refactoring.common.core.RefactoringContribution;

public class ConvertConstantsToEnumRefactoringContribution extends
		RefactoringContribution {

	public RefactoringDescriptor createDescriptor(String id, String project,
			String description, String comment, Map arguments, int flags) {
		return new ConvertConstantsToEnumDescriptor(project, description,
				comment, arguments);
	}
}