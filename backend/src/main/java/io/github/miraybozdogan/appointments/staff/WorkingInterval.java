package io.github.miraybozdogan.appointments.staff;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * A working interval on a day of the week, in the business's local time.
 * {@code start} is inclusive and {@code end} is exclusive.
 */
public record WorkingInterval(DayOfWeek dayOfWeek, LocalTime start, LocalTime end) {
}
