package edu.ohio_state.khatchad.refactoring;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "edu.ohio_state.khatchad.refactoring.messages"; //$NON-NLS-1$
	public static String ASTNodeProcessor_EncounteredBoxedExpression;
	public static String ASTNodeProcessor_IllegalArrayUpcast;
	public static String ASTNodeProcessor_IllegalAssignmentExpression;
	public static String ASTNodeProcessor_IllegalExpression;
	public static String ASTNodeProcessor_IllegalInfixExpression;
	public static String ASTNodeProcessor_IllegalNodeContext;
	public static String ASTNodeProcessor_NonEnumerizableTypeEncountered;
	public static String ASTNodeProcessor_SourceNotPresent;
	public static String ConvertConstantsToEnum_Name;
	public static String ConvertConstantsToEnumRefactoring_CheckingPreconditions;
	public static String ConvertConstantsToEnumRefactoring_CompilingSource;
	public static String ConvertConstantsToEnumRefactoring_CreatingChange;
	public static String ConvertConstantsToEnumRefactoring_CUContainsCompileErrors;
	public static String ConvertConstantsToEnumRefactoring_EnumTypeMustHaveCorrectVisibility;
	public static String ConvertConstantsToEnumRefactoring_FieldCannotBeExpressedAsEnum;
	public static String ConvertConstantsToEnumRefactoring_FieldIsBoolean;
	public static String ConvertConstantsToEnumRefactoring_FieldIsNotAConstant;
	public static String ConvertConstantsToEnumRefactoring_FieldMustBePrimitive;
	public static String ConvertConstantsToEnumRefactoring_FieldNotEligibleForEnum;
	public static String ConvertConstantsToEnumRefactoring_FieldsHaveNotBeenSpecified;
	public static String ConvertConstantsToEnumRefactoring_FileDoesNotExist;
	public static String ConvertConstantsToEnumRefactoring_PreconditionFailed;
	public static String ConvertConstantsToEnumRefactoring_RefactoringNotPossible;
	public static String ConvertConstantsToEnumRefactoring_WrongType;
	public static String EnumConstantComparator_BothObjectsMustMatch;
	public static String EnumConstantComparator_CannotCompare;
	public static String EnumConstantComparator_CannotCompareWithoutPrimitives;
	public static String Util_InvalidMemberVisibility;
	public static String Util_MemberNotFound;
	public static String Worklist_IllegalWorklistElement;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
