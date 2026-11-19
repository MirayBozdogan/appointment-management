package io.github.miraybozdogan.appointments.catalog;

import static org.assertj.core.api.Assertions.assertThat;

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
class CatalogApiTests {

	@Autowired
	MockMvcTester mvc;

	String businessId;

	@BeforeEach
	void createBusiness() {
		businessId = createBusiness("salon");
	}

	@Test
	void createsServicesAndListsThemByName() {
		assertThat(createService(businessId, """
				{"name": "Saç kesimi", "durationMinutes": 30, "priceCents": 50000}
				""")).hasStatus(HttpStatus.CREATED);
		assertThat(createService(businessId, """
				{"name": "Boya", "durationMinutes": 90, "priceCents": 150000}
				""")).hasStatus(HttpStatus.CREATED);

		assertThat(mvc.get().uri("/api/businesses/{id}/services", businessId))
				.hasStatusOk()
				.bodyJson()
				.extractingPath("$[*].name").asArray().containsExactly("Boya", "Saç kesimi");
	}

	@Test
	void rejectsDuplicateNameIgnoringCase() {
		assertThat(createService(businessId, """
				{"name": "Manikür", "durationMinutes": 45, "priceCents": 40000}
				""")).hasStatus(HttpStatus.CREATED);

		assertThat(createService(businessId, """
				{"name": "MANIKÜR", "durationMinutes": 60, "priceCents": 45000}
				""")).hasStatus(HttpStatus.CONFLICT);
	}

	@Test
	void updatesAndDeactivatesService() {
		String serviceId = id(createService(businessId, """
				{"name": "Fön", "durationMinutes": 20, "priceCents": 20000}
				"""));

		assertThat(mvc.put().uri("/api/businesses/{b}/services/{s}", businessId, serviceId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name": "Fön", "durationMinutes": 25, "priceCents": 25000, "active": false}
						"""))
				.hasStatusOk()
				.bodyJson()
				.extractingPath("$.active").isEqualTo(false);
	}

	@Test
	void doesNotExposeServicesOfAnotherBusiness() {
		String otherBusinessId = createBusiness("other");
		String serviceId = id(createService(otherBusinessId, """
				{"name": "Masaj", "durationMinutes": 60, "priceCents": 80000}
				"""));

		assertThat(mvc.get().uri("/api/businesses/{b}/services/{s}", businessId, serviceId))
				.hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void returnsNotFoundForUnknownBusiness() {
		assertThat(createService(UUID.randomUUID().toString(), """
				{"name": "Masaj", "durationMinutes": 60, "priceCents": 80000}
				""")).hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void rejectsTooShortDuration() {
		assertThat(createService(businessId, """
				{"name": "Hızlı", "durationMinutes": 1, "priceCents": 1000}
				""")).hasStatus(HttpStatus.BAD_REQUEST);
	}

	private String createBusiness(String prefix) {
		String slug = prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
		return id(mvc.post().uri("/api/businesses")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name": "Test", "slug": "%s", "timezone": "Europe/Istanbul"}
						""".formatted(slug))
				.exchange());
	}

	private MvcTestResult createService(String businessId, String json) {
		return mvc.post().uri("/api/businesses/{id}/services", businessId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json)
				.exchange();
	}

	private static String id(MvcTestResult result) {
		String location = result.getResponse().getHeader("Location");
		return location.substring(location.lastIndexOf('/') + 1);
	}

}
