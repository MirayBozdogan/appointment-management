package io.github.miraybozdogan.appointments.staff.internal;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

record AssignServicesRequest(@NotNull Set<@NotNull UUID> serviceIds) {
}
