package edu.ohio_state.khatchad.refactoring;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class RefactoringPlugin extends AbstractUIPlugin {

	private static RefactoringPlugin plugin;

	public static RefactoringPlugin getDefault() {
		return plugin;
	}

	public static void log(Throwable throwable) {
		getDefault().getLog().log(
				new Status(IStatus.ERROR,
						ConvertConstantsToEnumDescriptor.REFACTORING_ID, 0,
						throwable.getMessage(), throwable));
	}

	public RefactoringPlugin() {
		plugin = this;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
}