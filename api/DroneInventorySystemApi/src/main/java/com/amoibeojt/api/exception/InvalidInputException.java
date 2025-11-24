package com.amoibeojt.api.exception;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 入力値不正例外
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidInputException extends RuntimeException {
	private static final ResourceBundle messages = ResourceBundle.getBundle("messages");

	private final String resolvedMessage;

	public InvalidInputException(String messageKeyOrMessage, boolean treatAsKey) {
		super(messageKeyOrMessage);
		if (treatAsKey) {
			String resolved;
			try {
				resolved = messages.getString(messageKeyOrMessage);
			} catch (MissingResourceException e) {
				resolved = messageKeyOrMessage; // fallback to raw string if key not found
			}
			this.resolvedMessage = resolved;
		} else {
			this.resolvedMessage = messageKeyOrMessage;
		}
	}

	public InvalidInputException(String message) {
		this(message, false); // default: treat as raw message
	}

	@Override
	public String getMessage() {
		return resolvedMessage;
	}
}