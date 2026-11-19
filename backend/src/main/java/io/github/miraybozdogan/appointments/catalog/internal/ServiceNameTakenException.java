package io.github.miraybozdogan.appointments.catalog.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
class ServiceNameTakenException extends RuntimeException {

	ServiceNameTakenException(String name) {
		super("A service named '" + name + "' already exists in this business");
	}

}
