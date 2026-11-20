package io.github.miraybozdogan.appointments.booking;

/**
 * See the appointment state machine in docs/data-model.md.
 */
public enum AppointmentStatus {

	/** Booked by a customer. */
	BOOKED,

	/** Freed by a cancellation and reserved while it is offered to the waitlist. */
	HELD,

	CANCELLED,

	COMPLETED,

	/** A hold that no waitlist entry accepted; the slot is bookable again. */
	RELEASED

}
