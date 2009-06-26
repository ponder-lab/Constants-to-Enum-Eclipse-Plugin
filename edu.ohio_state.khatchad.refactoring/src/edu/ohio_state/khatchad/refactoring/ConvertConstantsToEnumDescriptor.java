package edu.ohio_state.khatchad.refactoring;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class ConvertConstantsToEnumDescriptor extends RefactoringDescriptor {

	public static final String REFACTORING_ID = "edu.ohio_state.khatchad.refactoring.convert.fields.to.enum";

	private final Map fArguments;

	public ConvertConstantsToEnumDescriptor(String project, String description,
			String comment, Map arguments) {
		super(REFACTORING_ID, project, description, comment,
				RefactoringDescriptor.STRUCTURAL_CHANGE
						| RefactoringDescriptor.MULTI_CHANGE);
		this.fArguments = arguments;
	}

	public Refactoring createRefactoring(RefactoringStatus status)
			throws CoreException {
		final ConvertConstantsToEnumRefactoring refactoring = new ConvertConstantsToEnumRefactoring();
		status.merge(refactoring.initialize(this.fArguments));
		return refactoring;
	}

	public Map getArguments() {
		return this.fArguments;
	}
}