package io.github.miraybozdogan.appointments.availability.internal;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/businesses/{businessId}/availability")
class AvailabilityController {

	private final AvailabilityService availability;

	AvailabilityController(AvailabilityService availability) {
		this.availability = availability;
	}

	@GetMapping
	DayAvailability freeSlots(@PathVariable UUID businessId, @RequestParam UUID staffId,
			@RequestParam UUID serviceId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return availability.freeSlots(businessId, staffId, serviceId, date);
	}

}
