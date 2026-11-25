package io.github.miraybozdogan.appointments.availability.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The staff member cannot be booked for this service at all (inactive, or does not perform it).
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
class AvailabilityRejectedException extends RuntimeException {

	AvailabilityRejectedException(String message) {
		super(message);
	}

}
