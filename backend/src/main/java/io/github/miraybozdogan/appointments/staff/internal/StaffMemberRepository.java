package io.github.miraybozdogan.appointments.staff.internal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface StaffMemberRepository extends JpaRepository<StaffMember, UUID> {

	// Loads service ids in the same query to avoid one extra query per staff member (N+1).
	@EntityGraph(attributePaths = "serviceIds")
	List<StaffMember> findByBusinessIdOrderByDisplayName(UUID businessId);

	Optional<StaffMember> findByIdAndBusinessId(UUID id, UUID businessId);

}
