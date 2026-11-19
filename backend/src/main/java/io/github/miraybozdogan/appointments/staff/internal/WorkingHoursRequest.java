package io.github.miraybozdogan.appointments.staff.internal;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import io.github.miraybozdogan.appointments.staff.WorkingInterval;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Replaces a staff member's whole week. Times are in the business's local time.
 */
record WorkingHoursRequest(@NotNull List<@NotNull @Valid Interval> intervals) {

	record Interval(@NotNull DayOfWeek dayOfWeek, @NotNull LocalTime start, @NotNull LocalTime end) {

		WorkingInterval toWorkingInterval() {
			return new WorkingInterval(dayOfWeek, start, end);
		}

	}

}
