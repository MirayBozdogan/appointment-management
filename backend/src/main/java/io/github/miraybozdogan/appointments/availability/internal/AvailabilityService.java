package io.github.miraybozdogan.appointments.availability.internal;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.github.miraybozdogan.appointments.booking.AppointmentView;
import io.github.miraybozdogan.appointments.booking.BookingApi;
import io.github.miraybozdogan.appointments.business.BusinessApi;
import io.github.miraybozdogan.appointments.catalog.CatalogApi;
import io.github.miraybozdogan.appointments.catalog.ServiceView;
import io.github.miraybozdogan.appointments.staff.StaffApi;
import io.github.miraybozdogan.appointments.staff.StaffView;
import io.github.miraybozdogan.appointments.staff.WorkingInterval;

/**
 * Lists the start times that booking would accept: the same 15-minute grid, working hours and
 * no-overlap rule, evaluated in the business's local time (ADR 0004).
 */
@Service
class AvailabilityService {

	private final BusinessApi businesses;

	private final CatalogApi catalog;

	private final StaffApi staff;

	private final BookingApi booking;

	private final Clock clock;

	AvailabilityService(BusinessApi businesses, CatalogApi catalog, StaffApi staff, BookingApi booking,
			Clock clock) {
		this.businesses = businesses;
		this.catalog = catalog;
		this.staff = staff;
		this.booking = booking;
		this.clock = clock;
	}

	DayAvailability freeSlots(UUID businessId, UUID staffId, UUID serviceId, LocalDate date) {
		ZoneId zone = businesses.get(businessId).timezone();
		ServiceView service = catalog.get(businessId, serviceId);
		StaffView member = staff.get(businessId, staffId);
		if (!service.active() || !member.active() || !member.serviceIds().contains(service.id())) {
			throw new AvailabilityRejectedException("Staff member is not bookable for this service");
		}

		Duration duration = Duration.ofMinutes(service.durationMinutes());
		Instant now = clock.instant();
		List<AppointmentView> taken = booking.activeAppointments(businessId, staffId,
				date.atStartOfDay(zone).toInstant(), date.plusDays(1).atStartOfDay(zone).toInstant());

		List<OffsetDateTime> slots = new ArrayList<>();
		for (WorkingInterval interval : staff.workingHours(businessId, staffId)) {
			if (interval.dayOfWeek() != date.getDayOfWeek()) {
				continue;
			}
			LocalDateTime intervalEnd = date.atTime(interval.end());
			// LocalDateTime, not LocalTime, so the loop cannot wrap around midnight.
			for (LocalDateTime local = firstGridTime(date.atTime(interval.start())); local
				.isBefore(intervalEnd); local = local.plusMinutes(BookingApi.SLOT_STEP_MINUTES)) {
				// A local time skipped by a daylight saving change does not exist; offer nothing.
				if (zone.getRules().getValidOffsets(local).isEmpty()) {
					continue;
				}
				Instant start = local.atZone(zone).toInstant();
				Instant end = start.plus(duration);
				if (LocalDateTime.ofInstant(end, zone).isAfter(intervalEnd) || !start.isAfter(now)
						|| overlapsAny(start, end, taken)) {
					continue;
				}
				slots.add(start.atZone(zone).toOffsetDateTime());
			}
		}
		return new DayAvailability(date, zone, slots);
	}

	/** Rounds up to the next 15-minute mark, so a shift starting at 09:10 offers 09:15 first. */
	private static LocalDateTime firstGridTime(LocalDateTime start) {
		int remainder = start.getMinute() % BookingApi.SLOT_STEP_MINUTES;
		return remainder == 0 ? start : start.plusMinutes(BookingApi.SLOT_STEP_MINUTES - remainder);
	}

	// Same half-open rule as the database: back-to-back appointments do not overlap.
	private static boolean overlapsAny(Instant start, Instant end, List<AppointmentView> taken) {
		return taken.stream().anyMatch(a -> start.isBefore(a.endAt()) && end.isAfter(a.startAt()));
	}

}
