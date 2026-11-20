package io.github.miraybozdogan.appointments.booking.internal;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/**
 * @param customerId temporary: will be taken from the authenticated user once
 *                   authentication exists, and removed from the request
 * @param startAt    start time with an offset, e.g. {@code 2026-11-23T10:00:00+03:00};
 *                   the end is computed from the service duration
 */
record BookAppointmentRequest(
		@NotNull UUID staffId,
		@NotNull UUID serviceId,
		@NotNull UUID customerId,
		@NotNull OffsetDateTime startAt) {
}
