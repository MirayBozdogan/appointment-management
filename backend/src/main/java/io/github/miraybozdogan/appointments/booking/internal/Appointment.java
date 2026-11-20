package io.github.miraybozdogan.appointments.booking.internal;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import io.github.miraybozdogan.appointments.booking.AppointmentStatus;
import io.github.miraybozdogan.appointments.booking.AppointmentView;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "appointment")
class Appointment {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	private UUID id;

	@Column(name = "business_id", updatable = false)
	private UUID businessId;

	@Column(name = "staff_id", updatable = false)
	private UUID staffId;

	@Column(name = "service_id", updatable = false)
	private UUID serviceId;

	@Column(name = "customer_id")
	private UUID customerId;

	@Column(name = "start_at", updatable = false)
	private Instant startAt;

	@Column(name = "end_at", updatable = false)
	private Instant endAt;

	@Enumerated(EnumType.STRING)
	private AppointmentStatus status;

	// Optimistic locking: two concurrent updates of the same row cannot both succeed.
	@Version
	private long version;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private Instant createdAt;

	@Column(name = "cancelled_at")
	private Instant cancelledAt;

	protected Appointment() {
		// for JPA
	}

	static Appointment book(UUID businessId, UUID staffId, UUID serviceId, UUID customerId, Instant startAt,
			Instant endAt) {
		Appointment appointment = new Appointment();
		appointment.businessId = businessId;
		appointment.staffId = staffId;
		appointment.serviceId = serviceId;
		appointment.customerId = customerId;
		appointment.startAt = startAt;
		appointment.endAt = endAt;
		appointment.status = AppointmentStatus.BOOKED;
		return appointment;
	}

	void cancel(Instant now) {
		if (status != AppointmentStatus.BOOKED) {
			throw new AppointmentNotCancellableException(id, status);
		}
		status = AppointmentStatus.CANCELLED;
		cancelledAt = now;
	}

	AppointmentView toView() {
		return new AppointmentView(id, businessId, staffId, serviceId, customerId, startAt, endAt, status);
	}

}
