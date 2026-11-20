package io.github.miraybozdogan.appointments.booking.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The requested time overlaps another active appointment of the same staff member.
 */
@ResponseStatus(HttpStatus.CONFLICT)
class SlotUnavailableException extends RuntimeException {

	SlotUnavailableException() {
		super("The requested time is no longer available");
	}

}
