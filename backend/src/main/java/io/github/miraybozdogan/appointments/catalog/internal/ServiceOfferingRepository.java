package io.github.miraybozdogan.appointments.catalog.internal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, UUID> {

	List<ServiceOffering> findByBusinessIdOrderByName(UUID businessId);

	Optional<ServiceOffering> findByIdAndBusinessId(UUID id, UUID businessId);

	boolean existsByBusinessIdAndNameIgnoreCase(UUID businessId, String name);

	boolean existsByBusinessIdAndNameIgnoreCaseAndIdNot(UUID businessId, String name, UUID id);

}
