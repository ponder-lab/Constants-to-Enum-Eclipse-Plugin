package edu.ohio_state.khatchad.refactoring;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jdt.core.IField;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class ConvertConstantsToEnumAction implements
		IWorkbenchWindowActionDelegate {

	/**
	 * The fields selected to be converted to enum.
	 */
	private final Collection selectedFields = new HashSet();
	
	/**
	 * The workbench window.
	 */
	private IWorkbenchWindow window;

	public void dispose() {
		// Do nothing
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	/*
	 * TODO: Just a simulation for testing purposes. Should really be using a UI
	 * wizard instead of forcing the change to create itself.
	 * Update: currently working on this -Raffi 7/30/09.
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (!this.selectedFields.isEmpty() && this.window != null) {

			final ConvertConstantsToEnumRefactoring refactoring = new ConvertConstantsToEnumRefactoring(
					this.selectedFields);
			/*
			 * TODO: This is the code to execute the actual refactoring. In the future, we will place this code inside the wizard.
			 */
			/*
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
				throw new RuntimeException(E); //TODO: Ugly.
			}
			*/
			run(new ConvertConstantsToEnumWizard(refactoring,
					Messages.ConvertConstantsToEnum_Name), this.window.getShell(),
					Messages.ConvertConstantsToEnum_Name);
		}
	}

	public void run(RefactoringWizard wizard, Shell parent, String dialogTitle) {
		try {
			final RefactoringWizardOpenOperation operation = new RefactoringWizardOpenOperation(
					wizard);
			operation.run(parent, dialogTitle);
		}
		catch (final InterruptedException exception) {
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