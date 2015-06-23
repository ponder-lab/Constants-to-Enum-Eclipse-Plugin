package edu.ohio_state.khatchad.refactoring.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.ohio_state.khatchad.refactoring.ConvertConstantsToEnumRefactoringPlugin;

public class ConvertConstantsToEnumHandler extends AbstractHandler {

	/**
	 * {@inheritDoc}
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection currentSelection = HandlerUtil
				.getCurrentSelectionChecked(event);

		List selectedFields = getFields(currentSelection);
		try {
			Shell shell = HandlerUtil.getActiveShellChecked(event);
			IField[] fields = (IField[]) selectedFields
					.toArray(new IField[] {});
			ConvertConstantsToEnumWizard
					.startConvertConstantsToEnumRefactoring(fields, shell);
		} catch (final JavaModelException exception) {
			ConvertConstantsToEnumRefactoringPlugin.getDefault().log(exception);
		}

		return null;
	}

	private List getFields(ISelection selection) {
		List fields = new ArrayList();
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Iterator iterator = structuredSelection.iterator();
			while (iterator.hasNext()) {
				Object selectedObject = iterator.next();
				if (selectedObject instanceof IField) {
					fields.add(selectedObject);
				} else if (selectedObject instanceof IType) {
					// need to traverse each of the fields of the selected
					// object.
					IType type = (IType) selectedObject;
					fields.addAll(getFields(type)); 
				} 
				
				//this condition check if the class compilationUnit get selected, it will convert all possible IFields to Enum
				else if (selectedObject instanceof ICompilationUnit) {
					// need to traverse each of the fields of the selected
					ICompilationUnit compilationType = (ICompilationUnit) selectedObject;
					IType[] compilationArray = null;
					try {
						compilationArray = compilationType.getAllTypes();
						//Adding a HasSet to remove duplicates
						Set hs = new HashSet();
						for (int i = 0; i < compilationArray.length; i++) {
							fields.addAll(getFields(compilationArray[i])); 
						}
						//removing duplicates
						hs.addAll(fields);
						fields.clear();
						fields.addAll(hs);
					} catch (JavaModelException e) {}
				}
				//Issue: Need a exact method to find the ITypes / Ifields Working 
				//this condition check if a package get selected, it will convert all possible IFields to Enum
				else if (selectedObject instanceof IPackageFragment) {
					// need to traverse each of the fields of the selected
					IPackageFragment projectFr = (IPackageFragment) selectedObject;
					IType[] jprojectsArray = null;
					try {
						jprojectsArray = ((ICompilationUnit) projectFr).getAllTypes();//need to get all the types or fields. get children not working
						for (int i = 0; i < jprojectsArray.length; i++) {
							
							fields.addAll(getFields(jprojectsArray[i])); 
						}
					} catch (JavaModelException e) {}
					
					
				}
			}
		}
		return fields;
	}

	/**
	 * @param type
	 * @param fields
	 */
	public List getFields(IType type) {
		List fields = new ArrayList();
		
		try {
			IField[] fieldsOfType = type.getFields();
			fields.addAll(Arrays.asList(fieldsOfType));
		} catch (JavaModelException e) {
		}
		
		//check for inner classes.
		try {
			IType[] innerTypes = type.getTypes();
			for (int i = 0; i < innerTypes.length; i++) {
				IType innerType = innerTypes[i];
				fields.addAll(getFields(innerType));
			}
		} catch (JavaModelException e) {
		}
		
		return fields;
	}

}
