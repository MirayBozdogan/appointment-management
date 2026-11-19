package io.github.miraybozdogan.appointments.business;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class BusinessNotFoundException extends RuntimeException {

	public BusinessNotFoundException(UUID id) {
		super("Business " + id + " not found");
	}

}
