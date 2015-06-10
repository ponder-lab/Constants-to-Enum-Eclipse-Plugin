/**
 * 
 */
package edu.ohio_state.khatchad.refactoring.core;

import edu.ohio_state.khatchad.refactoring.ConvertConstantsToEnumDescriptor;

/**
 * @author raffi
 * 
 */
public abstract class Status extends org.eclipse.core.runtime.Status {

	/**
	 * @param severity
	 * @param code
	 * @param message
	 * @param exception
	 */
	public Status(int severity, int code, String message,
			Throwable exception) {
		super(severity, ConvertConstantsToEnumDescriptor.REFACTORING_ID, code,
				message, exception);
	}

	/**
	 * @param severity
	 * @param message
	 */
	public Status(int severity, String message) {
		super(severity, ConvertConstantsToEnumDescriptor.REFACTORING_ID,
				message);
	}

	/**
	 * @param severity
	 * @param message
	 * @param exception
	 */
	public Status(int severity, String message, Throwable exception) {
		super(severity, ConvertConstantsToEnumDescriptor.REFACTORING_ID,
				message, exception);
	}
}