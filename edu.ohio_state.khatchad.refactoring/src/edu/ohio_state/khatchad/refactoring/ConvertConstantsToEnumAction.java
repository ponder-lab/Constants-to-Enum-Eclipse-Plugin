package edu.ohio_state.khatchad.refactoring;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import edu.ohio_state.khatchad.refactoring.core.InternalStateStatus;

public class ConvertConstantsToEnumAction implements
		IWorkbenchWindowActionDelegate {

	/**
	 * The fields selected to be converted to enum.
	 */
	private final Collection selectedFields = new HashSet();

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}

	/*
	 * TODO: Just a simulation for testing purposes. Should really be using a UI
	 * wizard instead of forcing the change to create itself.
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (!this.selectedFields.isEmpty()) {
			final ConvertConstantsToEnumRefactoring refactoring = new ConvertConstantsToEnumRefactoring(
					this.selectedFields);
			try {
				final RefactoringStatus status = refactoring
						.checkInitialConditions(new NullProgressMonitor());
				status.merge(refactoring
						.checkFinalConditions(new NullProgressMonitor()));
				if (status.hasFatalError())
					throw new CoreException(
							new InternalStateStatus(
									IStatus.ERROR,
									status
											.getMessageMatchingSeverity(RefactoringStatus.FATAL)));
				final Change change = refactoring
						.createChange(new NullProgressMonitor());
				change.perform(new NullProgressMonitor());
			} catch (final CoreException E) {
				throw new RuntimeException(E);
			}
			/*
			 * TODO: Create UI Wizad here.
			 */
			// run(new ConvertFieldsToEnumWizard(refactoring,
			// "Convert Constants to Enum"), this.window.getShell(),
			// "Convert Constants to Enum");
		}
	}

	public void run(RefactoringWizard wizard, Shell parent, String dialogTitle) {
		try {
			final RefactoringWizardOpenOperation operation = new RefactoringWizardOpenOperation(
					wizard);
			operation.run(parent, dialogTitle);
		} catch (final InterruptedException exception) {
			// Do nothing
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selectedFields.clear();
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			for (final java.util.Iterator it = structuredSelection.toList()
					.iterator(); it.hasNext();) {
				final Object obj = it.next();
				if (obj instanceof IField)
					this.selectedFields.add(obj);
			}
		}
	}
}