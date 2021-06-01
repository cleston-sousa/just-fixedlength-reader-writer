package br.net.gits.exception;

import java.util.HashMap;
import java.util.Map;

public class ReadException extends RuntimeException {

	private static final long serialVersionUID = 4839499054045608016L;

	Map<String, String> fieldErrors = new HashMap<>();

	public ReadException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReadException(String message) {
		super(message);
	}

	public ReadException(String message, Map<String, String> fieldErrors) {
		super(message);
		this.fieldErrors = fieldErrors;
	}

}
