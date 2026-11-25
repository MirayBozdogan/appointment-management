package io.github.miraybozdogan.appointments.booking.internal;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.miraybozdogan.appointments.booking.AppointmentNotFoundException;
import io.github.miraybozdogan.appointments.booking.AppointmentStatus;
import io.github.miraybozdogan.appointments.booking.AppointmentView;
import io.github.miraybozdogan.appointments.booking.BookingApi;
import io.github.miraybozdogan.appointments.business.BusinessApi;
import io.github.miraybozdogan.appointments.business.BusinessView;
import io.github.miraybozdogan.appointments.catalog.CatalogApi;
import io.github.miraybozdogan.appointments.catalog.ServiceView;
import io.github.miraybozdogan.appointments.staff.StaffApi;
import io.github.miraybozdogan.appointments.staff.StaffView;
import io.github.miraybozdogan.appointments.staff.WorkingInterval;

@Service
@Transactional(readOnly = true)
class BookingService implements BookingApi {

	/** Statuses that occupy a slot; must match the WHERE clause of appointment_no_overlap. */
	private static final Set<AppointmentStatus> ACTIVE_STATUSES =
			EnumSet.of(AppointmentStatus.BOOKED, AppointmentStatus.HELD);

	private static final String NO_OVERLAP_CONSTRAINT = "appointment_no_overlap";

	private final AppointmentRepository appointments;

	private final BusinessApi businesses;

	private final CatalogApi catalog;

	private final StaffApi staff;

	private final Clock clock;

	BookingService(AppointmentRepository appointments, BusinessApi businesses, CatalogApi catalog, StaffApi staff,
			Clock clock) {
		this.appointments = appointments;
		this.businesses = businesses;
		this.catalog = catalog;
		this.staff = staff;
		this.clock = clock;
	}

	AppointmentView get(UUID businessId, UUID appointmentId) {
		return find(businessId, appointmentId).toView();
	}

	@Transactional
	AppointmentView book(UUID businessId, BookAppointmentRequest request) {
		BusinessView business = businesses.get(businessId);
		ServiceView service = catalog.get(businessId, request.serviceId());
		StaffView member = staff.get(businessId, request.staffId());

		if (!service.active()) {
			throw new BookingRejectedException("Service is not active");
		}
		if (!member.active()) {
			throw new BookingRejectedException("Staff member is not active");
		}
		if (!member.serviceIds().contains(service.id())) {
			throw new BookingRejectedException("Staff member does not perform this service");
		}

		Instant start = request.startAt().toInstant();
		Instant end = start.plus(Duration.ofMinutes(service.durationMinutes()));
		if (!start.isAfter(clock.instant())) {
			throw new BookingRejectedException("Appointments must start in the future");
		}
		checkGridAndWorkingHours(business.timezone(), start, end,
				staff.workingHours(businessId, member.id()));

		Appointment appointment = Appointment.book(businessId, member.id(), service.id(), request.customerId(),
				start, end);
		try {
			// Flush now so a constraint violation surfaces here, inside this method.
			return appointments.saveAndFlush(appointment).toView();
		}
		catch (DataIntegrityViolationException e) {
			if (violates(e, NO_OVERLAP_CONSTRAINT)) {
				throw new SlotUnavailableException();
			}
			throw e;
		}
	}

	@Override
	public List<AppointmentView> activeAppointments(UUID businessId, UUID staffId, Instant from, Instant to) {
		return appointments.findOverlapping(businessId, staffId, ACTIVE_STATUSES, from, to).stream()
				.map(Appointment::toView)
				.toList();
	}

	@Transactional
	AppointmentView cancel(UUID businessId, UUID appointmentId) {
		Appointment appointment = find(businessId, appointmentId);
		appointment.cancel(clock.instant());
		return appointment.toView();
	}

	/**
	 * The appointment must start on the 15-minute grid and fit completely inside one working
	 * interval, both evaluated in the business's local time (ADR 0004).
	 */
	private static void checkGridAndWorkingHours(ZoneId zone, Instant start, Instant end,
			List<WorkingInterval> workingHours) {
		LocalDateTime localStart = LocalDateTime.ofInstant(start, zone);
		LocalDateTime localEnd = LocalDateTime.ofInstant(end, zone);

		if (localStart.getMinute() % SLOT_STEP_MINUTES != 0 || localStart.getSecond() != 0
				|| localStart.getNano() != 0) {
			throw new BookingRejectedException("Appointments start every " + SLOT_STEP_MINUTES + " minutes");
		}

		boolean fits = localStart.toLocalDate().equals(localEnd.toLocalDate()) && workingHours.stream()
				.anyMatch(interval -> interval.dayOfWeek() == localStart.getDayOfWeek()
						&& !localStart.toLocalTime().isBefore(interval.start())
						&& !localEnd.toLocalTime().isAfter(interval.end()));
		if (!fits) {
			throw new BookingRejectedException("Outside the staff member's working hours");
		}
	}

	private static boolean violates(DataIntegrityViolationException e, String constraint) {
		return e.getCause() instanceof ConstraintViolationException cve
				&& constraint.equals(cve.getConstraintName());
	}

	private Appointment find(UUID businessId, UUID appointmentId) {
		return appointments.findByIdAndBusinessId(appointmentId, businessId)
				.orElseThrow(() -> new AppointmentNotFoundException(appointmentId));
	}

}
