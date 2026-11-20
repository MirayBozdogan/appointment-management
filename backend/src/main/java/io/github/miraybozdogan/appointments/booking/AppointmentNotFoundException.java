package io.github.miraybozdogan.appointments.booking;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AppointmentNotFoundException extends RuntimeException {

	public AppointmentNotFoundException(UUID appointmentId) {
		super("Appointment " + appointmentId + " not found");
	}

}
