package io.github.miraybozdogan.appointments.catalog;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ServiceNotFoundException extends RuntimeException {

	public ServiceNotFoundException(UUID serviceId) {
		super("Service " + serviceId + " not found");
	}

}
