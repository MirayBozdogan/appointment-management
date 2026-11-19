package io.github.miraybozdogan.appointments.staff.internal;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import io.github.miraybozdogan.appointments.staff.StaffView;
import io.github.miraybozdogan.appointments.staff.WorkingInterval;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "staff_member")
class StaffMember {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	private UUID id;

	@Column(name = "business_id", updatable = false)
	private UUID businessId;

	@Column(name = "user_id")
	private UUID userId;

	@Column(name = "display_name")
	private String displayName;

	private boolean active;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private Instant createdAt;

	@ElementCollection
	@CollectionTable(name = "staff_service", joinColumns = @JoinColumn(name = "staff_id"))
	@Column(name = "service_id")
	private Set<UUID> serviceIds = new HashSet<>();

	@ElementCollection
	@CollectionTable(name = "working_hours", joinColumns = @JoinColumn(name = "staff_id"))
	private Set<WorkingHoursEntry> workingHours = new HashSet<>();

	protected StaffMember() {
		// for JPA
	}

	StaffMember(UUID businessId, String displayName) {
		this.businessId = businessId;
		this.displayName = displayName;
		this.active = true;
	}

	void replaceServices(Set<UUID> serviceIds) {
		this.serviceIds.clear();
		this.serviceIds.addAll(serviceIds);
	}

	/**
	 * Replaces the whole week. Callers must have validated that intervals do not overlap.
	 */
	void replaceWorkingHours(List<WorkingInterval> intervals) {
		this.workingHours.clear();
		intervals.forEach(i -> this.workingHours.add(new WorkingHoursEntry(i.dayOfWeek(), i.start(), i.end())));
	}

	List<WorkingInterval> workingIntervals() {
		return workingHours.stream()
				.map(WorkingHoursEntry::toInterval)
				.sorted(Comparator.comparing(WorkingInterval::dayOfWeek).thenComparing(WorkingInterval::start))
				.toList();
	}

	StaffView toView() {
		return new StaffView(id, businessId, displayName, active, Set.copyOf(serviceIds));
	}

}
