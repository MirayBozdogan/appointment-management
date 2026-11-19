package io.github.miraybozdogan.appointments.staff;

import java.util.Set;
import java.util.UUID;

public record StaffView(UUID id, UUID businessId, String displayName, boolean active, Set<UUID> serviceIds) {
}
