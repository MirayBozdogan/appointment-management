package io.github.miraybozdogan.appointments.booking.internal;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.github.miraybozdogan.appointments.booking.AppointmentStatus;

@ResponseStatus(HttpStatus.CONFLICT)
class AppointmentNotCancellableException extends RuntimeException {

	AppointmentNotCancellableException(UUID appointmentId, AppointmentStatus status) {
		super("Appointment " + appointmentId + " cannot be cancelled in status " + status);
	}

}
