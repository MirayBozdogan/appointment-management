package io.github.miraybozdogan.appointments.business.internal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

record CreateBusinessRequest(
		@NotBlank @Size(max = 120) String name,
		@NotBlank @Size(max = 60) @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$",
				message = "must contain only lowercase letters, digits and single hyphens") String slug,
		@NotBlank String timezone,
		@Min(1) @Max(1440) Integer offerTtlMinutes) {
}
