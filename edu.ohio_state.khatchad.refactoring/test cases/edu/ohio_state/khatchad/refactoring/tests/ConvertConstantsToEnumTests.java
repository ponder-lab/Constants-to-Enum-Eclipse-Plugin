/**
 * 
 */
package edu.ohio_state.khatchad.refactoring.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/*
 * Uncomment the following for test cases:
 */
//import org.eclipse.jdt.ui.tests.refactoring.Java15Setup;
//import org.eclipse.jdt.ui.tests.refactoring.RefactoringTest;

/**
 * @author raffi
 *
 */
public class ConvertConstantsToEnumTests //extends RefactoringTest 
{
	private static final Class clazz= ConvertConstantsToEnumTests.class;
	
	private static final String REFACTORING_PATH = "ConvertConstantsToEnum/"; //$NON-NLS-1$
	
	public static Test suite() {
//		return new Java15Setup(new TestSuite(clazz));
		return null;
	}
	
	public static Test setUpTest(Test someTest) {
//		return new Java15Setup(someTest);
		return null;
	}
	
	public ConvertConstantsToEnumTests(String name) {
//		super(name);
	}
	
	protected String getRefactoringPath() {
		return REFACTORING_PATH;
	}
}