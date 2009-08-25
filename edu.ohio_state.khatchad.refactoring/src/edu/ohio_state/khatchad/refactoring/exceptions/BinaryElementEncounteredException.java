package edu.ohio_state.khatchad.refactoring.exceptions;

import org.eclipse.jdt.core.IJavaElement;

public class BinaryElementEncounteredException extends
		DefinitelyNotEnumerizableException {
	private static final long serialVersionUID = 8044384732970184194L;

	private final IJavaElement problemElement;

	public BinaryElementEncounteredException(String message,
			IJavaElement problemElement) {
		super(message, null);
		this.problemElement = problemElement;
	}

	public String toString() {
		final StringBuffer ret = new StringBuffer();

		ret.append(this.problemElement.getJavaProject().getProject().getName());
		ret.append("\t"); //$NON-NLS-1$
		ret.append(this.problemElement.getHandleIdentifier());
		ret.append("\t"); //$NON-NLS-1$
		ret.append(-1);
		ret.append("\t"); //$NON-NLS-1$
		ret.append(-1);
		ret.append("\t"); //$NON-NLS-1$
		ret.append(-1);
		ret.append("\t"); //$NON-NLS-1$
		ret.append(this.getClass().getName());
		ret.append("\t"); //$NON-NLS-1$
		ret.append(this.getMessage());
		ret.append("\t"); //$NON-NLS-1$
		ret.append(this.problemElement.getClass().getName());
		ret.append("\t"); //$NON-NLS-1$
		ret.append(this.problemElement);
		ret.append("\t\t\t"); //$NON-NLS-1$

		return ret.toString();
	}
}
