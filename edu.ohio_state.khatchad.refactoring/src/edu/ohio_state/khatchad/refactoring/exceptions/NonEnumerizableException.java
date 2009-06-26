package edu.ohio_state.khatchad.refactoring.exceptions;

public abstract class NonEnumerizableException extends RuntimeException {
	public NonEnumerizableException(String message) {
		super(message);
	}
}
