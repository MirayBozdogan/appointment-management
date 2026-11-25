package io.github.miraybozdogan.appointments.booking;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Public API of the booking module, used by other modules.
 */
public interface BookingApi {

	/** Appointments start on a 15-minute grid in the business's local time. */
	int SLOT_STEP_MINUTES = 15;

	/**
	 * Active (booked or held) appointments of a staff member that overlap the time range
	 * from {@code from} (inclusive) to {@code to} (exclusive), ordered by start time.
	 */
	List<AppointmentView> activeAppointments(UUID businessId, UUID staffId, Instant from, Instant to);

}
