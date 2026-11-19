package io.github.miraybozdogan.appointments.catalog;

import java.util.UUID;

public record ServiceView(UUID id, UUID businessId, String name, int durationMinutes, int priceCents,
		boolean active) {
}
