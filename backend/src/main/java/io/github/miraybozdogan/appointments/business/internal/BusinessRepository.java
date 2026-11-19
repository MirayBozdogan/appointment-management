package io.github.miraybozdogan.appointments.business.internal;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface BusinessRepository extends JpaRepository<Business, UUID> {

	boolean existsBySlug(String slug);

}
