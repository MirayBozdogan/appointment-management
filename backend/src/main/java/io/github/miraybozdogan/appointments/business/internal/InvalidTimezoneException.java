package io.github.miraybozdogan.appointments.business.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidTimezoneException extends RuntimeException {

	InvalidTimezoneException(String value) {
		super("'" + value + "' is not a valid IANA time zone");
	}

}
