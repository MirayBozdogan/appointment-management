package io.github.miraybozdogan.appointments.booking;

import java.time.Instant;
import java.util.UUID;

public record AppointmentView(UUID id, UUID businessId, UUID staffId, UUID serviceId, UUID customerId,
		Instant startAt, Instant endAt, AppointmentStatus status) {
}
