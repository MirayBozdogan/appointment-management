package io.github.miraybozdogan.appointments.business;

import java.time.ZoneId;
import java.util.UUID;

public record BusinessView(UUID id, String name, String slug, ZoneId timezone, int offerTtlMinutes) {
}
