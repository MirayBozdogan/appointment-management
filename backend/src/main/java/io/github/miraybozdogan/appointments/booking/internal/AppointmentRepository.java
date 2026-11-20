package io.github.miraybozdogan.appointments.booking.internal;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

	Optional<Appointment> findByIdAndBusinessId(UUID id, UUID businessId);

}
