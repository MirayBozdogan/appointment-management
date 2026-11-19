package io.github.miraybozdogan.appointments.staff.internal;

import java.time.DayOfWeek;
import java.time.LocalTime;

import io.github.miraybozdogan.appointments.staff.WorkingInterval;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;

/**
 * One row of the {@code working_hours} table.
 */
@Embeddable
record WorkingHoursEntry(
		@Convert(converter = IsoDayOfWeekConverter.class) @Column(name = "day_of_week") DayOfWeek dayOfWeek,
		@Column(name = "start_local") LocalTime start,
		@Column(name = "end_local") LocalTime end) {

	WorkingInterval toInterval() {
		return new WorkingInterval(dayOfWeek, start, end);
	}

}
