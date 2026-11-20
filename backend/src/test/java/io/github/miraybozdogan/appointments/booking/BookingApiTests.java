package io.github.miraybozdogan.appointments.booking;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
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
class BookingApiTests {

	static final ZoneId ISTANBUL = ZoneId.of("Europe/Istanbul");

	/** A day one week ahead; staff work 09:00-18:00 every day, so the weekday does not matter. */
	static final LocalDate DAY = LocalDate.now(ISTANBUL).plusDays(7);

	@Autowired
	MockMvcTester mvc;

	String businessId;

	String serviceId;

	String staffId;

	@BeforeEach
	void setUp() {
		businessId = createBusiness();
		serviceId = id(post("/api/businesses/" + businessId + "/services", """
				{"name": "Saç kesimi", "durationMinutes": 30, "priceCents": 50000}
				"""));
		staffId = id(post("/api/businesses/" + businessId + "/staff", """
				{"displayName": "Ayşe"}
				"""));
		put("/api/businesses/" + businessId + "/staff/" + staffId + "/services", """
				{"serviceIds": ["%s"]}
				""".formatted(serviceId));
		put("/api/businesses/" + businessId + "/staff/" + staffId + "/working-hours", everyDay("09:00", "18:00"));
	}

	@Test
	void booksAppointmentInsideWorkingHours() {
		MvcTestResult result = book(at("10:00"));

		assertThat(result).hasStatus(HttpStatus.CREATED);
		assertThat(result).bodyJson().extractingPath("$.status").isEqualTo("BOOKED");
		assertThat(result).bodyJson().extractingPath("$.endAt")
				.isEqualTo(at("10:30").toInstant().toString());
	}

	@Test
	void rejectsOverlappingAppointmentButAllowsBackToBack() {
		assertThat(book(at("10:00"))).hasStatus(HttpStatus.CREATED);

		assertThat(book(at("10:15"))).hasStatus(HttpStatus.CONFLICT);
		assertThat(book(at("10:30"))).hasStatus(HttpStatus.CREATED);
	}

	@Test
	void rejectsAppointmentEndingAfterWorkingHours() {
		assertThat(book(at("17:45"))).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
	}

	@Test
	void rejectsStartOffTheFifteenMinuteGrid() {
		assertThat(book(at("10:05"))).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
	}

	@Test
	void rejectsAppointmentInThePast() {
		OffsetDateTime yesterday = DAY.minusDays(8).atTime(LocalTime.parse("10:00")).atZone(ISTANBUL)
				.toOffsetDateTime();
		assertThat(book(yesterday)).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
	}

	@Test
	void rejectsStaffWhoDoesNotPerformTheService() {
		String otherServiceId = id(post("/api/businesses/" + businessId + "/services", """
				{"name": "Boya", "durationMinutes": 60, "priceCents": 150000}
				"""));

		assertThat(post("/api/businesses/" + businessId + "/appointments", """
				{"staffId": "%s", "serviceId": "%s", "customerId": "%s", "startAt": "%s"}
				""".formatted(staffId, otherServiceId, UUID.randomUUID(), at("10:00"))))
				.hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
	}

	@Test
	void cancellingFreesTheSlotAndCannotBeRepeated() {
		String appointmentId = id(book(at("11:00")));
		String cancelUri = "/api/businesses/" + businessId + "/appointments/" + appointmentId + "/cancel";

		assertThat(mvc.post().uri(cancelUri)).hasStatusOk()
				.bodyJson().extractingPath("$.status").isEqualTo("CANCELLED");
		assertThat(book(at("11:00"))).hasStatus(HttpStatus.CREATED);
		assertThat(mvc.post().uri(cancelUri)).hasStatus(HttpStatus.CONFLICT);
	}

	@Test
	void doesNotExposeAppointmentsOfAnotherBusiness() {
		String appointmentId = id(book(at("12:00")));
		String otherBusinessId = createBusiness();

		assertThat(mvc.get().uri("/api/businesses/{b}/appointments/{a}", otherBusinessId, appointmentId))
				.hasStatus(HttpStatus.NOT_FOUND);
	}

	private MvcTestResult book(OffsetDateTime startAt) {
		return post("/api/businesses/" + businessId + "/appointments", """
				{"staffId": "%s", "serviceId": "%s", "customerId": "%s", "startAt": "%s"}
				""".formatted(staffId, serviceId, UUID.randomUUID(), startAt));
	}

	private static OffsetDateTime at(String localTime) {
		return DAY.atTime(LocalTime.parse(localTime)).atZone(ISTANBUL).toOffsetDateTime();
	}

	static String everyDay(String start, String end) {
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

	private String createBusiness() {
		String slug = "booking-" + UUID.randomUUID().toString().substring(0, 8);
		return id(post("/api/businesses", """
				{"name": "Test", "slug": "%s", "timezone": "Europe/Istanbul"}
				""".formatted(slug)));
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
