package edu.ohio_state.khatchad.refactoring;

import java.util.Map;

import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

public class ConvertConstantsToEnumRefactoringContribution extends
		RefactoringContribution {

	public RefactoringDescriptor createDescriptor(String id, String project,
			String description, String comment, Map arguments, int flags) {
		return new ConvertConstantsToEnumDescriptor(project, description,
				comment, arguments);
	}

	public Map retrieveArgumentMap(RefactoringDescriptor descriptor) {
		if (descriptor instanceof ConvertConstantsToEnumDescriptor)
			return ((ConvertConstantsToEnumDescriptor) descriptor)
					.getArguments();
		return super.retrieveArgumentMap(descriptor);
	}
}