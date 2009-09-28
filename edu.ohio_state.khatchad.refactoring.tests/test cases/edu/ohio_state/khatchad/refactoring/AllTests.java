package edu.ohio_state.khatchad.refactoring;

import edu.ohio_state.khatchad.refactoring.ui.ConvertToEnumTypePageTests;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for edu.ohio_state.khatchad.refactoring.tests");
		//$JUnit-BEGIN$
//		suite.addTest(ConvertConstantsToEnumTests.suite());
		suite.addTest(ConvertToEnumTypePageTests.suite());
		//$JUnit-END$
		return suite;
	}

}
