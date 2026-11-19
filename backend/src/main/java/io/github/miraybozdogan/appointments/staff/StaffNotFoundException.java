package io.github.miraybozdogan.appointments.staff;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class StaffNotFoundException extends RuntimeException {

	public StaffNotFoundException(UUID staffId) {
		super("Staff member " + staffId + " not found");
	}

}
