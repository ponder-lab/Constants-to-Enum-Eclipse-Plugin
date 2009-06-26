/**
 * 
 */
package edu.ohio_state.khatchad.refactoring.core;

import org.eclipse.core.runtime.Status;

import edu.ohio_state.khatchad.refactoring.ConvertConstantsToEnumDescriptor;

/**
 * @author raffi
 * 
 */
public abstract class CoreStatus extends Status {

	/**
	 * @param severity
	 * @param code
	 * @param message
	 * @param exception
	 */
	public CoreStatus(int severity, int code, String message,
			Throwable exception) {
		super(severity, ConvertConstantsToEnumDescriptor.REFACTORING_ID, code,
				message, exception);
	}

	/**
	 * @param severity
	 * @param message
	 */
	public CoreStatus(int severity, String message) {
		super(severity, ConvertConstantsToEnumDescriptor.REFACTORING_ID,
				message);
	}

	/**
	 * @param severity
	 * @param message
	 * @param exception
	 */
	public CoreStatus(int severity, String message, Throwable exception) {
		super(severity, ConvertConstantsToEnumDescriptor.REFACTORING_ID,
				message, exception);
	}
}