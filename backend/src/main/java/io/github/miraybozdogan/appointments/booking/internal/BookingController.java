package io.github.miraybozdogan.appointments.booking.internal;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.miraybozdogan.appointments.booking.AppointmentView;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/businesses/{businessId}/appointments")
class BookingController {

	private final BookingService booking;

	BookingController(BookingService booking) {
		this.booking = booking;
	}

	@PostMapping
	ResponseEntity<AppointmentView> book(@PathVariable UUID businessId,
			@Valid @RequestBody BookAppointmentRequest request) {
		AppointmentView created = booking.book(businessId, request);
		URI location = URI.create("/api/businesses/" + businessId + "/appointments/" + created.id());
		return ResponseEntity.created(location).body(created);
	}

	@GetMapping("/{appointmentId}")
	AppointmentView get(@PathVariable UUID businessId, @PathVariable UUID appointmentId) {
		return booking.get(businessId, appointmentId);
	}

	// A state transition, not a deletion: the row stays for history and the waitlist.
	@PostMapping("/{appointmentId}/cancel")
	AppointmentView cancel(@PathVariable UUID businessId, @PathVariable UUID appointmentId) {
		return booking.cancel(businessId, appointmentId);
	}

}
