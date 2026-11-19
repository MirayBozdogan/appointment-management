package io.github.miraybozdogan.appointments.business;

import static org.assertj.core.api.Assertions.assertThat;

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
class BusinessApiTests {

	@Autowired
	MockMvcTester mvc;

	@Test
	void createsBusinessAndReturnsItById() {
		MvcTestResult created = create("""
				{"name": "Kuaför Ayşe", "slug": "kuafor-ayse", "timezone": "Europe/Istanbul"}
				""");

		assertThat(created).hasStatus(HttpStatus.CREATED);
		assertThat(created).bodyJson().extractingPath("$.offerTtlMinutes").isEqualTo(15);
		String location = created.getResponse().getHeader("Location");

		assertThat(mvc.get().uri(location))
				.hasStatusOk()
				.bodyJson()
				.extractingPath("$.timezone").isEqualTo("Europe/Istanbul");
	}

	@Test
	void rejectsDuplicateSlug() {
		String body = """
				{"name": "Studio One", "slug": "studio-one", "timezone": "Europe/Berlin"}
				""";
		assertThat(create(body)).hasStatus(HttpStatus.CREATED);

		assertThat(create(body)).hasStatus(HttpStatus.CONFLICT);
	}

	@Test
	void rejectsUnknownTimezone() {
		assertThat(create("""
				{"name": "Nowhere", "slug": "nowhere", "timezone": "Mars/Olympus"}
				""")).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	void rejectsInvalidSlug() {
		assertThat(create("""
				{"name": "Bad Slug", "slug": "Bad Slug!", "timezone": "Europe/Istanbul"}
				""")).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	void returnsNotFoundForUnknownId() {
		assertThat(mvc.get().uri("/api/businesses/00000000-0000-0000-0000-000000000000"))
				.hasStatus(HttpStatus.NOT_FOUND);
	}

	private MvcTestResult create(String json) {
		return mvc.post().uri("/api/businesses")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json)
				.exchange();
	}

}
