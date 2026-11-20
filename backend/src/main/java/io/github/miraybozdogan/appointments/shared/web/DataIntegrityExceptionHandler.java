package io.github.miraybozdogan.appointments.shared.web;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Safety net for database constraint violations that application checks did not catch,
 * for example two concurrent requests that both passed an "exists?" check.
 * <p>
 * Without this handler they would surface as {@code 500 Internal Server Error}.
 */
@RestControllerAdvice
class DataIntegrityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(DataIntegrityExceptionHandler.class);

	// PostgreSQL SQLSTATE codes, see https://www.postgresql.org/docs/current/errcodes-appendix.html
	static final String UNIQUE_VIOLATION = "23505";

	static final String EXCLUSION_VIOLATION = "23P01";

	static final String FOREIGN_KEY_VIOLATION = "23503";

	@ExceptionHandler(DataIntegrityViolationException.class)
	ProblemDetail handle(DataIntegrityViolationException e) {
		String sqlState = sqlState(e);
		// The constraint name is useful for debugging but is not shown to API clients.
		log.debug("Constraint violation (SQLSTATE {})", sqlState, e);

		return switch (sqlState) {
			case UNIQUE_VIOLATION, EXCLUSION_VIOLATION -> ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
					"The request conflicts with existing data.");
			case FOREIGN_KEY_VIOLATION -> ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
					"The request refers to data that does not exist or is still in use.");
			default -> ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
					"The request violates a data rule.");
		};
	}

	/**
	 * Walks the cause chain to the JDBC exception, which carries the SQLSTATE code.
	 */
	private static String sqlState(Throwable e) {
		for (Throwable cause = e; cause != null; cause = cause.getCause()) {
			if (cause instanceof SQLException sql && sql.getSQLState() != null) {
				return sql.getSQLState();
			}
		}
		return "";
	}

}
