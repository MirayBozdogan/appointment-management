package io.github.miraybozdogan.appointments.booking.internal;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.github.miraybozdogan.appointments.booking.AppointmentStatus;

interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

	Optional<Appointment> findByIdAndBusinessId(UUID id, UUID businessId);

	// Two intervals overlap when each one starts before the other ends.
	@Query("""
			select a from Appointment a
			where a.businessId = :businessId and a.staffId = :staffId
			  and a.status in :statuses
			  and a.startAt < :to and a.endAt > :from
			order by a.startAt""")
	List<Appointment> findOverlapping(UUID businessId, UUID staffId, Collection<AppointmentStatus> statuses,
			Instant from, Instant to);

}
