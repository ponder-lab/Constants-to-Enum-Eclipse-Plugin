package edu.ohio_state.khatchad.refactoring;

import java.util.Map;

import edu.cuny.citytech.refactoring.common.Refactoring;
import edu.cuny.citytech.refactoring.common.RefactoringDescriptor;

public class ConvertConstantsToEnumDescriptor extends RefactoringDescriptor {

	public static final String REFACTORING_ID = "edu.ohio_state.khatchad.refactoring.convert.fields.to.enum"; //$NON-NLS-1$

	public ConvertConstantsToEnumDescriptor(String project, String description,
			String comment, Map arguments) {
		super(REFACTORING_ID, project, description, comment, arguments);
	}
	
	public Refactoring createRefactoring() {
		return new ConvertConstantsToEnumRefactoring();
	}
}