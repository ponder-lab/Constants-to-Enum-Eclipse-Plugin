package edu.ohio_state.khatchad.refactoring.exceptions;

public abstract class NonEnumerizableException extends RuntimeException {
	
	/**
	 * Generated serial version UID.
	 */
	private static final long serialVersionUID = 5421178242046723079L;

	public NonEnumerizableException(String message) {
		super(message);
	}
}
