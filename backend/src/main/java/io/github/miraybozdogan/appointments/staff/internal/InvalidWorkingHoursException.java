package io.github.miraybozdogan.appointments.staff.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidWorkingHoursException extends RuntimeException {

	InvalidWorkingHoursException(String message) {
		super(message);
	}

}
