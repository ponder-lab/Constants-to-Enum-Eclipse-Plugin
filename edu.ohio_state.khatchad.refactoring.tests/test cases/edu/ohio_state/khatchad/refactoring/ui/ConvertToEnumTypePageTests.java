package edu.ohio_state.khatchad.refactoring.ui;

import java.util.ArrayList;

import edu.ohio_state.khatchad.refactoring.ConvertConstantsToEnumRefactoring;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ConvertToEnumTypePageTests extends TestCase {

	private ConvertToEnumTypePage page;

	protected void setUp() throws Exception {
		super.setUp();
		page = new ConvertToEnumTypePage("test");
		page.setWizard(new ConvertConstantsToEnumWizard(new ConvertConstantsToEnumRefactoring(new ArrayList()), "test"));
	}
	
	public void testSuggestedName() throws Exception {
		String suggestedName = page.getSuggestedEnumTypeName(null);
		assertEquals("", suggestedName);
		
		suggestedName = page.getSuggestedEnumTypeName(new String[] { "COLOR_FOO"});
		assertEquals("", suggestedName);
		
		suggestedName = page.getSuggestedEnumTypeName(new String[] { "COLOR_FOO", "COLOR_BAR" });
		assertEquals("Color", suggestedName);
		
		suggestedName = page.getSuggestedEnumTypeName(new String[] { "COLOR__FOO", "COLOR__BAR" });
		assertEquals("Color", suggestedName);
		
		suggestedName = page.getSuggestedEnumTypeName(new String[] { "SYSTEM_COLOR_FOO", "SYSTEM_COLOR_BAR" });
		assertEquals("SystemColor", suggestedName);
		
		suggestedName = page.getSuggestedEnumTypeName(new String[] { "COLORFOO", "COLORBAR" });
		assertEquals("Color", suggestedName);
		
		suggestedName = page.getSuggestedEnumTypeName(new String[] { "FOO", "BAR" });
		assertEquals("", suggestedName);
	}
	
	public static Test suite() {
		return new TestSuite(ConvertToEnumTypePageTests.class);
	}
}
