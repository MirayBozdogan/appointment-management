package io.github.miraybozdogan.appointments.availability;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.zone.ZoneOffsetTransition;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import io.github.miraybozdogan.appointments.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class AvailabilityApiTests {

	static final ZoneId ISTANBUL = ZoneId.of("Europe/Istanbul");

	/** A day one week ahead; working hours are set for every day, so the weekday does not matter. */
	static final LocalDate DAY = LocalDate.now(ISTANBUL).plusDays(7);

	@Autowired
	MockMvcTester mvc;

	@Test
	void listsStartTimesWhereTheWholeServiceFitsInWorkingHours() {
		Setup s = setUp("09:00", "10:00", 30);

		assertThat(availability(s, DAY)).hasStatusOk().bodyJson().extractingPath("$.slots")
				.asArray().containsExactly((Object[]) localTimes("09:00", "09:15", "09:30"));
	}

	@Test
	void excludesStartTimesThatOverlapAnAppointmentButKeepsBackToBack() {
		Setup s = setUp("09:00", "11:00", 30);
		book(s, at("09:30"));

		assertThat(availability(s, DAY)).hasStatusOk().bodyJson().extractingPath("$.slots")
				.asArray().containsExactly((Object[]) localTimes("09:00", "10:00", "10:15", "10:30"));
	}

	@Test
	void returnsTheBusinessTimezoneAndNoSlotsForAPastDay() {
		Setup s = setUp("09:00", "18:00", 30);

		MvcTestResult result = availability(s, LocalDate.now(ISTANBUL).minusDays(1));
		assertThat(result).hasStatusOk().bodyJson().extractingPath("$.timezone").isEqualTo("Europe/Istanbul");
		assertThat(result).bodyJson().extractingPath("$.slots").asArray().isEmpty();
	}

	@Test
	void rejectsServiceTheStaffMemberDoesNotPerform() {
		Setup s = setUp("09:00", "18:00", 30);
		String otherServiceId = id(post("/api/businesses/" + s.businessId + "/services", """
				{"name": "Boya", "durationMinutes": 60, "priceCents": 150000}
				"""));

		assertThat(mvc.get().uri("/api/businesses/{b}/availability?staffId={s}&serviceId={v}&date={d}",
				s.businessId, s.staffId, otherServiceId, DAY)).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
	}

	@Test
	void rejectsMissingDate() {
		Setup s = setUp("09:00", "18:00", 30);

		assertThat(mvc.get().uri("/api/businesses/{b}/availability?staffId={s}&serviceId={v}",
				s.businessId, s.staffId, s.serviceId)).hasStatus(HttpStatus.BAD_REQUEST);
	}

	/**
	 * Berlin skips 02:00-03:00 local time on the last Sunday of March (ADR 0004). Slots before the
	 * change have offset +01:00, slots after it +02:00, the lost hour offers nothing, and booking
	 * accepts a slot listed after the change.
	 */
	@Test
	void skipsTheLocalHourLostToDaylightSavingTime() {
		LocalDate day = nextSpringForward(ZoneId.of("Europe/Berlin")).getDateTimeBefore().toLocalDate();
		Setup s = setUp("Europe/Berlin", "01:00", "04:00", 30);

		assertThat(availability(s, day)).hasStatusOk().bodyJson().extractingPath("$.slots").asArray()
				.containsExactly(day + "T01:00:00+01:00", day + "T01:15:00+01:00", day + "T01:30:00+01:00",
						day + "T01:45:00+01:00", day + "T03:00:00+02:00", day + "T03:15:00+02:00",
						day + "T03:30:00+02:00");
		book(s, OffsetDateTime.parse(day + "T03:00:00+02:00"));
	}

	record Setup(String businessId, String serviceId, String staffId) {
	}

	private Setup setUp(String start, String end, int durationMinutes) {
		return setUp("Europe/Istanbul", start, end, durationMinutes);
	}

	private Setup setUp(String timezone, String start, String end, int durationMinutes) {
		String slug = "availability-" + UUID.randomUUID().toString().substring(0, 8);
		String businessId = id(post("/api/businesses", """
				{"name": "Test", "slug": "%s", "timezone": "%s"}
				""".formatted(slug, timezone)));
		String serviceId = id(post("/api/businesses/" + businessId + "/services", """
				{"name": "Saç kesimi", "durationMinutes": %d, "priceCents": 50000}
				""".formatted(durationMinutes)));
		String staffId = id(post("/api/businesses/" + businessId + "/staff", """
				{"displayName": "Ayşe"}
				"""));
		put("/api/businesses/" + businessId + "/staff/" + staffId + "/services", """
				{"serviceIds": ["%s"]}
				""".formatted(serviceId));
		put("/api/businesses/" + businessId + "/staff/" + staffId + "/working-hours", everyDay(start, end));
		return new Setup(businessId, serviceId, staffId);
	}

	private MvcTestResult availability(Setup s, LocalDate date) {
		return mvc.get().uri("/api/businesses/{b}/availability?staffId={s}&serviceId={v}&date={d}",
				s.businessId, s.staffId, s.serviceId, date).exchange();
	}

	private void book(Setup s, OffsetDateTime startAt) {
		assertThat(post("/api/businesses/" + s.businessId + "/appointments", """
				{"staffId": "%s", "serviceId": "%s", "customerId": "%s", "startAt": "%s"}
				""".formatted(s.staffId, s.serviceId, UUID.randomUUID(), startAt))).hasStatus(HttpStatus.CREATED);
	}

	/** The next change where clocks jump forward, at least a day away so its slots are in the future. */
	private static ZoneOffsetTransition nextSpringForward(ZoneId zone) {
		ZoneOffsetTransition transition = zone.getRules().nextTransition(Instant.now().plus(Duration.ofDays(1)));
		while (!transition.isGap()) {
			transition = zone.getRules().nextTransition(transition.getInstant());
		}
		return transition;
	}

	private static OffsetDateTime at(String localTime) {
		return DAY.atTime(LocalTime.parse(localTime)).atZone(ISTANBUL).toOffsetDateTime();
	}

	/** The JSON strings the API returns for these local times on {@link #DAY}. */
	private static String[] localTimes(String... localTimes) {
		String[] expected = new String[localTimes.length];
		for (int i = 0; i < localTimes.length; i++) {
			expected[i] = at(localTimes[i]).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		}
		return expected;
	}

	private static String everyDay(String start, String end) {
		StringBuilder intervals = new StringBuilder();
		for (String day : new String[] { "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY",
				"SUNDAY" }) {
			if (!intervals.isEmpty()) {
				intervals.append(',');
			}
			intervals.append("""
					{"dayOfWeek": "%s", "start": "%s", "end": "%s"}""".formatted(day, start, end));
		}
		return "{\"intervals\": [" + intervals + "]}";
	}

	private MvcTestResult post(String uri, String json) {
		return mvc.post().uri(uri).contentType(MediaType.APPLICATION_JSON).content(json).exchange();
	}

	private void put(String uri, String json) {
		assertThat(mvc.put().uri(uri).contentType(MediaType.APPLICATION_JSON).content(json)).hasStatusOk();
	}

	private static String id(MvcTestResult result) {
		String location = result.getResponse().getHeader("Location");
		return location.substring(location.lastIndexOf('/') + 1);
	}

}
