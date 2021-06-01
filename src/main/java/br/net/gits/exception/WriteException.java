package br.net.gits.exception;

public class WriteException extends RuntimeException {

	private static final long serialVersionUID = 6243951977780229951L;

	public WriteException() {
		super();
	}

	public WriteException(String message, Throwable cause) {
		super(message, cause);
	}

	public WriteException(String message) {
		super(message);
	}

}
