package io.github.miraybozdogan.appointments.staff;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import io.github.miraybozdogan.appointments.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class StaffApiTests {

	@Autowired
	MockMvcTester mvc;

	@Autowired
	JdbcClient jdbc;

	String businessId;

	@BeforeEach
	void setUp() {
		businessId = createBusiness();
	}

	@Test
	void createsStaffAndListsThemByName() {
		createStaff(businessId, "Zeynep");
		createStaff(businessId, "Ali");

		assertThat(mvc.get().uri("/api/businesses/{b}/staff", businessId))
				.hasStatusOk()
				.bodyJson()
				.extractingPath("$[*].displayName").asArray().containsExactly("Ali", "Zeynep");
	}

	@Test
	void assignsServicesOfTheSameBusiness() {
		String staffId = createStaff(businessId, "Ayşe");
		String serviceId = createService(businessId, "Saç kesimi");

		assertThat(put("/api/businesses/{b}/staff/{s}/services", """
				{"serviceIds": ["%s"]}
				""".formatted(serviceId), businessId, staffId))
				.hasStatusOk()
				.bodyJson()
				.extractingPath("$.serviceIds").asArray().containsExactly(serviceId);
	}

	@Test
	void rejectsServiceOfAnotherBusiness() {
		String staffId = createStaff(businessId, "Ayşe");
		String foreignServiceId = createService(createBusiness(), "Masaj");

		assertThat(put("/api/businesses/{b}/staff/{s}/services", """
				{"serviceIds": ["%s"]}
				""".formatted(foreignServiceId), businessId, staffId))
				.hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void replacesWorkingHoursIncludingASplitDay() {
		String staffId = createStaff(businessId, "Mehmet");

		assertThat(put("/api/businesses/{b}/staff/{s}/working-hours", """
				{"intervals": [
				  {"dayOfWeek": "MONDAY", "start": "13:00", "end": "18:00"},
				  {"dayOfWeek": "MONDAY", "start": "09:00", "end": "12:00"},
				  {"dayOfWeek": "TUESDAY", "start": "10:00", "end": "16:00"}
				]}
				""", businessId, staffId)).hasStatusOk();

		assertThat(mvc.get().uri("/api/businesses/{b}/staff/{s}/working-hours", businessId, staffId))
				.hasStatusOk()
				.bodyJson()
				.extractingPath("$[*].start").asArray().containsExactly("09:00:00", "13:00:00", "10:00:00");

		// A second PUT replaces the whole week. The new Monday interval overlaps the old ones,
		// so this also proves the old rows are deleted before the new ones are inserted.
		assertThat(put("/api/businesses/{b}/staff/{s}/working-hours", """
				{"intervals": [{"dayOfWeek": "MONDAY", "start": "10:00", "end": "15:00"}]}
				""", businessId, staffId))
				.hasStatusOk()
				.bodyJson()
				.extractingPath("$[*].start").asArray().containsExactly("10:00:00");
	}

	@Test
	void rejectsOverlappingWorkingHours() {
		String staffId = createStaff(businessId, "Mehmet");

		assertThat(put("/api/businesses/{b}/staff/{s}/working-hours", """
				{"intervals": [
				  {"dayOfWeek": "MONDAY", "start": "09:00", "end": "12:00"},
				  {"dayOfWeek": "MONDAY", "start": "11:00", "end": "14:00"}
				]}
				""", businessId, staffId)).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	void rejectsEndBeforeStart() {
		String staffId = createStaff(businessId, "Mehmet");

		assertThat(put("/api/businesses/{b}/staff/{s}/working-hours", """
				{"intervals": [{"dayOfWeek": "MONDAY", "start": "18:00", "end": "09:00"}]}
				""", businessId, staffId)).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	void databaseRejectsOverlappingWorkingHoursEvenWithoutTheApplicationCheck() {
		UUID staffId = UUID.fromString(createStaff(businessId, "Mehmet"));
		insertWorkingHours(staffId, "09:00", "12:00");

		assertThatThrownBy(() -> insertWorkingHours(staffId, "11:00", "14:00"))
				.isInstanceOf(DataIntegrityViolationException.class)
				.hasMessageContaining("working_hours_no_overlap");
	}

	@Test
	void doesNotExposeStaffOfAnotherBusiness() {
		String foreignStaffId = createStaff(createBusiness(), "Can");

		assertThat(mvc.get().uri("/api/businesses/{b}/staff/{s}", businessId, foreignStaffId))
				.hasStatus(HttpStatus.NOT_FOUND);
	}

	private void insertWorkingHours(UUID staffId, String start, String end) {
		jdbc.sql("INSERT INTO working_hours (staff_id, day_of_week, start_local, end_local) "
				+ "VALUES (?, 1, ?::time, ?::time)")
				.params(staffId, start, end)
				.update();
	}

	private String createBusiness() {
		String slug = "staff-test-" + UUID.randomUUID().toString().substring(0, 8);
		return id(post("/api/businesses", """
				{"name": "Test", "slug": "%s", "timezone": "Europe/Istanbul"}
				""".formatted(slug)));
	}

	private String createStaff(String businessId, String name) {
		return id(post("/api/businesses/" + businessId + "/staff", """
				{"displayName": "%s"}
				""".formatted(name)));
	}

	private String createService(String businessId, String name) {
		return id(post("/api/businesses/" + businessId + "/services", """
				{"name": "%s", "durationMinutes": 30, "priceCents": 50000}
				""".formatted(name)));
	}

	private MvcTestResult post(String uri, String json) {
		return mvc.post().uri(uri).contentType(MediaType.APPLICATION_JSON).content(json).exchange();
	}

	private MvcTestResult put(String uri, String json, Object... vars) {
		return mvc.put().uri(uri, vars).contentType(MediaType.APPLICATION_JSON).content(json).exchange();
	}

	private static String id(MvcTestResult result) {
		String location = result.getResponse().getHeader("Location");
		return location.substring(location.lastIndexOf('/') + 1);
	}

}
