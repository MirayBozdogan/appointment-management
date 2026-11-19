package io.github.miraybozdogan.appointments.staff;

import java.util.List;
import java.util.UUID;

/**
 * Public API of the staff module, used by other modules.
 */
public interface StaffApi {

	/**
	 * Returns a staff member only if they belong to the given business.
	 *
	 * @throws StaffNotFoundException if the staff member does not exist in this business
	 */
	StaffView get(UUID businessId, UUID staffId);

	/**
	 * Weekly working hours in the business's local time, sorted by day and start time.
	 *
	 * @throws StaffNotFoundException if the staff member does not exist in this business
	 */
	List<WorkingInterval> workingHours(UUID businessId, UUID staffId);

}
