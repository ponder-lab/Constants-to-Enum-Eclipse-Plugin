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

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;

import edu.ohio_state.khatchad.refactoring.ConvertConstantsToEnumRefactoringPlugin;

// TODO [bm] most stuff copied from JavaPluginImages
//           should be merged back later
public class RefactoringPluginImages {

	public static final IPath ICONS_PATH= new Path("$nl$/icons/full"); //$NON-NLS-1$
	
	private static final String T_WIZBAN= "wizban"; 	//$NON-NLS-1$
	
	public static final ImageDescriptor DESC_WIZBAN_REFACTOR_CONVERT_CONSTANTS_ENUM= createUnManaged(T_WIZBAN, "constantstoenum_wiz.png");	//$NON-NLS-1$
	
	/*
	 * Creates an image descriptor for the given prefix and name in the JDT UI bundle. The path can
	 * contain variables like $NL$.
	 * If no image could be found, the 'missing image descriptor' is returned.
	 */
	private static ImageDescriptor createUnManaged(String prefix, String name) {
		IPath path= ICONS_PATH.append(prefix).append(name);
		URL url= FileLocator.find(ConvertConstantsToEnumRefactoringPlugin.getDefault().getBundle(), path, null);
		if (url != null) {
			return ImageDescriptor.createFromURL(url);
		}
		return ImageDescriptor.getMissingImageDescriptor();
	}
}
