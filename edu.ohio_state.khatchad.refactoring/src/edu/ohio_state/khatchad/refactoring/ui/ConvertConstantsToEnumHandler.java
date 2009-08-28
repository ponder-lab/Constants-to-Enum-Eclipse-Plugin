package edu.ohio_state.khatchad.refactoring.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.ohio_state.khatchad.refactoring.ConvertConstantsToEnumRefactoring;
import edu.ohio_state.khatchad.refactoring.ConvertConstantsToEnumWizard;
import edu.ohio_state.khatchad.refactoring.Messages;

public class ConvertConstantsToEnumHandler extends AbstractHandler {

	/*
	 * TODO: Just a simulation for testing purposes. Should really be using a UI
	 * wizard instead of forcing the change to create itself.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands
	 * .ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection currentSelection = HandlerUtil
				.getCurrentSelectionChecked(event);

		List selectedFields = getSelectedFields(currentSelection);

		final ConvertConstantsToEnumRefactoring refactoring = new ConvertConstantsToEnumRefactoring(
				selectedFields);
		run(new ConvertConstantsToEnumWizard(refactoring,
				Messages.ConvertConstantsToEnum_Name), HandlerUtil
				.getActiveShellChecked(event),
				Messages.ConvertConstantsToEnum_Name);

		return null;
		/*
		 * TODO: This is the code to execute the actual refactoring. In the
		 * future, we will place this code inside the wizard.
		 */
		/*
		 * try { final RefactoringStatus status = refactoring
		 * .checkInitialConditions(new NullProgressMonitor());
		 * status.merge(refactoring .checkFinalConditions(new
		 * NullProgressMonitor())); if (status.hasFatalError()) throw new
		 * CoreException( new InternalStateStatus( IStatus.ERROR, status
		 * .getMessageMatchingSeverity(RefactoringStatus.FATAL))); final Change
		 * change = refactoring .createChange(new NullProgressMonitor());
		 * change.perform(new NullProgressMonitor()); } catch (final
		 * CoreException E) { throw new RuntimeException(E); //TODO: Ugly. }
		 */
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

	private List getSelectedFields(ISelection selection) {
		List selectedFields = new ArrayList();
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Iterator iterator = structuredSelection.iterator();
			while (iterator.hasNext()) {
				Object selectedObject = iterator.next();
				if (selectedObject instanceof IField) {
					selectedFields.add(selectedObject);
				}
			}
		}
		return selectedFields;
	}

}
