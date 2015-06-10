/**
 * 
 */
package edu.ohio_state.khatchad.refactoring.core;

/**
 * @author raffi
 * 
 */
public class InternalStateStatus extends Status {

	/**
	 * @param severity
	 * @param code
	 * @param message
	 * @param exception
	 */
	public InternalStateStatus(int severity, int code, String message,
			Throwable exception) {
		super(severity, code, message, exception);
	}

	/**
	 * @param severity
	 * @param message
	 */
	public InternalStateStatus(int severity, String message) {
		super(severity, message);
	}

	/**
	 * @param severity
	 * @param message
	 * @param exception
	 */
	public InternalStateStatus(int severity, String message, Throwable exception) {
		super(severity, message, exception);
	}
}