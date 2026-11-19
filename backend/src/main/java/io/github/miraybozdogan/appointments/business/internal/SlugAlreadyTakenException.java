package io.github.miraybozdogan.appointments.business.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
class SlugAlreadyTakenException extends RuntimeException {

	SlugAlreadyTakenException(String slug) {
		super("Slug '" + slug + "' is already taken");
	}

}
