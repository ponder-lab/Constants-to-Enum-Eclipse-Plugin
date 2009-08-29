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

import java.text.MessageFormat;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.internal.ui.dialogs.TextFieldNavigationHandler;
import org.eclipse.jdt.internal.ui.util.SWTUtil;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import edu.ohio_state.khatchad.refactoring.ConvertConstantsToEnumRefactoring;
import edu.ohio_state.khatchad.refactoring.Messages;

public class ConvertToEnumTypePage extends UserInputWizardPage {

	private Text fNameField;
	private Label fLabel;
	private Label fStatusLine;
	private CheckboxTableViewer fTableViewer;
	private final ILabelProvider fLabelProvider= new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT | JavaElementLabelProvider.SHOW_SMALL_ICONS);;

	public ConvertToEnumTypePage(String name) {
		super(name);
		setDescription(Messages.ConvertToEnumTypePage_Description);
	}

	/**
	 * {@inheritDoc}
	 */
	public void createControl(Composite parent) {
		final Composite composite= new Composite(parent, SWT.NONE);
		final GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		composite.setLayout(layout);

		createEnumTypeField(composite);
		createSpacer(composite);
		createFieldTableLabel(composite);
		createFieldTableComposite(composite);
		createStatusLine(composite);
		
		setControl(composite);
		TextFieldNavigationHandler.install(fNameField);
	}

	private void createEnumTypeField(final Composite parent) {
		final Label label= new Label(parent, SWT.NONE);
		label.setText(Messages.ConvertToEnumTypePage_EnumName);
		label.setLayoutData(new GridData());

		fNameField= new Text(parent, SWT.BORDER);
		fNameField.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
//				handleNameChanged(fNameField.getText());
			}
		});
		fNameField.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
	}

	protected void createFieldTableLabel(final Composite parent) {
		fLabel= new Label(parent, SWT.NONE);
		fLabel.setText(Messages.ConvertToEnumTypePage_ConstantsToExtract);
		final GridData data= new GridData();
		data.horizontalSpan= 2;
		fLabel.setLayoutData(data);
	}

	protected void createFieldTableComposite(final Composite parent) {
		final Composite composite= new Composite(parent, SWT.NONE);
		final GridData data= new GridData(GridData.FILL_BOTH);
		data.horizontalSpan= 2;
		composite.setLayoutData(data);
		final GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		composite.setLayout(layout);

		createMemberTable(composite);
	}

	private void createMemberTable(final Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		composite.setLayout(layout);
		
		final Table table= new Table(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK);
		table.setLinesVisible(true);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		table.setLayoutData(gridData);

		final GridData gd= new GridData(GridData.FILL_BOTH);
		gd.heightHint= SWTUtil.getTableHeightHint(table, 6);
		composite.setLayoutData(gd);

		fTableViewer= new CheckboxTableViewer(table);
		fTableViewer.setUseHashlookup(true);
		fTableViewer.setContentProvider(new ArrayContentProvider());
		fTableViewer.setLabelProvider(fLabelProvider);
		fTableViewer.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(final CheckStateChangedEvent event) {
				updateWizardPage(null, true);
			}
		});

		setTableInput();
	}

	private void setTableInput() {
		fTableViewer.setInput(getConvertToEnumRefactoring().getFieldsToRefactor().toArray(new IField[]{}));
	}

	private ConvertConstantsToEnumRefactoring getConvertToEnumRefactoring() {
		return (ConvertConstantsToEnumRefactoring) getRefactoring();
	}

	
	protected void createSpacer(final Composite parent) {
		final Label label= new Label(parent, SWT.NONE);
		final GridData data= new GridData();
		data.horizontalSpan= 2;
		data.heightHint= convertHeightInCharsToPixels(1) / 2;
		label.setLayoutData(data);
	}

	protected void createStatusLine(final Composite composite) {
		fStatusLine= new Label(composite, SWT.NONE);
		final GridData data= new GridData(SWT.FILL, SWT.TOP, false, false);
		data.horizontalSpan= 2;
		updateStatusLine();
		fStatusLine.setLayoutData(data);
	}

	private void updateStatusLine() {
		if (fStatusLine == null)
			return;
		final int selected= fTableViewer.getCheckedElements().length;
		final String[] keys= { String.valueOf(selected)};
		final String msg= MessageFormat.format(Messages.ConvertToEnumTypePage_CountMembersSelected, keys);
		fStatusLine.setText(msg);
	}
	
	private void updateWizardPage(final ISelection selection, final boolean displayErrors) {
		fTableViewer.refresh();
		if (selection != null) {
			fTableViewer.getControl().setFocus();
			fTableViewer.setSelection(selection);
		}
//		checkPageCompletionStatus(displayErrors);
		updateStatusLine();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setVisible(final boolean visible) {
		super.setVisible(visible);
		if (visible) {
			fNameField.setFocus();
		}
	}
}
