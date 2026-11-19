package io.github.miraybozdogan.appointments.staff.internal;

import java.time.DayOfWeek;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Stores {@link DayOfWeek} as its ISO-8601 number (1 = Monday ... 7 = Sunday).
 * The default ordinal mapping would store 0..6, which is easy to misread in SQL.
 */
@Converter
class IsoDayOfWeekConverter implements AttributeConverter<DayOfWeek, Integer> {

	@Override
	public Integer convertToDatabaseColumn(DayOfWeek day) {
		return day == null ? null : day.getValue();
	}

	@Override
	public DayOfWeek convertToEntityAttribute(Integer value) {
		return value == null ? null : DayOfWeek.of(value);
	}

}
