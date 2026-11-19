package io.github.miraybozdogan.appointments.catalog.internal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Body for creating and updating a service. {@code active} is only used on update.
 */
record ServiceRequest(
		@NotBlank @Size(max = 120) String name,
		@NotNull @Min(5) @Max(600) Integer durationMinutes,
		@NotNull @Min(0) Integer priceCents,
		Boolean active) {
}
