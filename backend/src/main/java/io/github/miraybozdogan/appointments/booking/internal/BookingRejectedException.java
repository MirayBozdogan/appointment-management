package io.github.miraybozdogan.appointments.booking.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The request is well-formed but breaks a booking rule (outside working hours, in the past, ...).
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
class BookingRejectedException extends RuntimeException {

	BookingRejectedException(String message) {
		super(message);
	}

}
