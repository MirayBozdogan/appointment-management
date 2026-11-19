package io.github.miraybozdogan.appointments.staff.internal;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.miraybozdogan.appointments.business.BusinessApi;
import io.github.miraybozdogan.appointments.catalog.CatalogApi;
import io.github.miraybozdogan.appointments.staff.StaffApi;
import io.github.miraybozdogan.appointments.staff.StaffNotFoundException;
import io.github.miraybozdogan.appointments.staff.StaffView;
import io.github.miraybozdogan.appointments.staff.WorkingInterval;

@Service
@Transactional(readOnly = true)
class StaffService implements StaffApi {

	private final StaffMemberRepository staff;

	private final BusinessApi businesses;

	private final CatalogApi catalog;

	StaffService(StaffMemberRepository staff, BusinessApi businesses, CatalogApi catalog) {
		this.staff = staff;
		this.businesses = businesses;
		this.catalog = catalog;
	}

	@Override
	public StaffView get(UUID businessId, UUID staffId) {
		return find(businessId, staffId).toView();
	}

	@Override
	public List<WorkingInterval> workingHours(UUID businessId, UUID staffId) {
		return find(businessId, staffId).workingIntervals();
	}

	List<StaffView> list(UUID businessId) {
		businesses.get(businessId); // 404 if the business does not exist
		return staff.findByBusinessIdOrderByDisplayName(businessId).stream()
				.map(StaffMember::toView)
				.toList();
	}

	@Transactional
	StaffView create(UUID businessId, CreateStaffRequest request) {
		businesses.get(businessId);
		StaffMember member = new StaffMember(businessId, request.displayName().trim());
		return staff.save(member).toView();
	}

	@Transactional
	StaffView assignServices(UUID businessId, UUID staffId, AssignServicesRequest request) {
		StaffMember member = find(businessId, staffId);
		// Every service must belong to the same business; otherwise CatalogApi throws 404.
		request.serviceIds().forEach(serviceId -> catalog.get(businessId, serviceId));
		member.replaceServices(request.serviceIds());
		return member.toView();
	}

	@Transactional
	List<WorkingInterval> replaceWorkingHours(UUID businessId, UUID staffId, WorkingHoursRequest request) {
		StaffMember member = find(businessId, staffId);
		List<WorkingInterval> intervals = request.intervals().stream()
				.map(WorkingHoursRequest.Interval::toWorkingInterval)
				.sorted(Comparator.comparing(WorkingInterval::dayOfWeek).thenComparing(WorkingInterval::start))
				.toList();
		validate(intervals);
		member.replaceWorkingHours(intervals);
		return member.workingIntervals();
	}

	/**
	 * Expects intervals sorted by day and start time. The database enforces the same rules;
	 * this check exists to return a clear 400 instead of a constraint violation.
	 */
	private static void validate(List<WorkingInterval> intervals) {
		WorkingInterval previous = null;
		for (WorkingInterval current : intervals) {
			if (!current.start().isBefore(current.end())) {
				throw new InvalidWorkingHoursException(
						"Start must be before end: " + current.dayOfWeek() + " " + current.start() + "-" + current.end());
			}
			if (previous != null && previous.dayOfWeek() == current.dayOfWeek()
					&& current.start().isBefore(previous.end())) {
				throw new InvalidWorkingHoursException("Overlapping intervals on " + current.dayOfWeek());
			}
			previous = current;
		}
	}

	private StaffMember find(UUID businessId, UUID staffId) {
		return staff.findByIdAndBusinessId(staffId, businessId)
				.orElseThrow(() -> new StaffNotFoundException(staffId));
	}

}
