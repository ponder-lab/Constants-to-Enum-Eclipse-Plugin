/*******************************************************************************
 * Copyright (c) 2009 Benjamin Muskalla and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Benjamin Muskalla - initial API and implementation
 *******************************************************************************/
package edu.ohio_state.khatchad.refactoring.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.cuny.citytech.refactoring.common.RefactoringPlugin;


public class ConvertConstantsToEnumHandler extends AbstractHandler {

	/**
	 * {@inheritDoc}
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection currentSelection = HandlerUtil
				.getCurrentSelectionChecked(event);

		List selectedFields = getSelectedFields(currentSelection);
		try {
			Shell shell = HandlerUtil.getActiveShellChecked(event);
			IField[] fields = (IField[]) selectedFields.toArray(new IField[] {});
			ConvertConstantsToEnumWizard.startConvertConstantsToEnumRefactoring(fields, shell);
		} catch (final JavaModelException exception) {
			RefactoringPlugin.getDefault().log(exception);
		}

		return null;
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
