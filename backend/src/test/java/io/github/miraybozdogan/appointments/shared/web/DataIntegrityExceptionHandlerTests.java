package io.github.miraybozdogan.appointments.shared.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

class DataIntegrityExceptionHandlerTests {

	private final DataIntegrityExceptionHandler handler = new DataIntegrityExceptionHandler();

	@Test
	void uniqueViolationBecomesConflict() {
		assertThat(handler.handle(violation("23505")).getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
	}

	@Test
	void exclusionViolationBecomesConflict() {
		assertThat(handler.handle(violation("23P01")).getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
	}

	@Test
	void foreignKeyViolationBecomesConflict() {
		assertThat(handler.handle(violation("23503")).getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
	}

	@Test
	void otherViolationsBecomeBadRequest() {
		assertThat(handler.handle(violation("23514")).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	private static DataIntegrityViolationException violation(String sqlState) {
		// Spring wraps the driver's SQLException; the handler must find it in the cause chain.
		return new DataIntegrityViolationException("could not execute statement",
				new RuntimeException(new SQLException("constraint violated", sqlState)));
	}

}
