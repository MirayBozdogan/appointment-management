package io.github.miraybozdogan.appointments.staff.internal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record CreateStaffRequest(@NotBlank @Size(max = 120) String displayName) {
}
