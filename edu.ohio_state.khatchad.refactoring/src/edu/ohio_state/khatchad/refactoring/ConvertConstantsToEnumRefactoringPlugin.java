package edu.ohio_state.khatchad.refactoring;

import org.osgi.framework.BundleContext;

import edu.cuny.citytech.refactoring.common.RefactoringPlugin;


public class ConvertConstantsToEnumRefactoringPlugin extends RefactoringPlugin {

	private static ConvertConstantsToEnumRefactoringPlugin plugin;
	
	public static RefactoringPlugin getDefault() {
		return plugin;
	}

	public void start(BundleContext context) throws Exception {
		plugin = this;
		super.start(context);
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/* (non-Javadoc)
	 * @see edu.cuny.citytech.refactoring.common.RefactoringPlugin#getRefactoringId()
	 */
	protected String getRefactoringId() {
		return ConvertConstantsToEnumDescriptor.REFACTORING_ID;
	}
}