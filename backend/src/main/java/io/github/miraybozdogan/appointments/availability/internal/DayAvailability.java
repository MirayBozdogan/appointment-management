package io.github.miraybozdogan.appointments.availability.internal;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Free start times for one day, with the business's offset so clients can show local time.
 */
record DayAvailability(LocalDate date, ZoneId timezone, List<OffsetDateTime> slots) {
}
