package io.github.miraybozdogan.appointments.booking;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import io.github.miraybozdogan.appointments.TestcontainersConfiguration;

/**
 * The central guarantee of the project: when many customers try to book the same slot at the
 * same moment, exactly one succeeds. Runs over real HTTP against a real PostgreSQL, so each
 * request has its own thread, transaction and database connection.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ConcurrentBookingTests {

	static final int CONCURRENT_REQUESTS = 20;

	@LocalServerPort
	int port;

	@Autowired
	JdbcClient jdbc;

	final HttpClient http = HttpClient.newHttpClient();

	@Test
	void onlyOneOfManyConcurrentBookingsForTheSameSlotSucceeds() throws Exception {
		String businessId = createBusiness();
		String serviceId = id(send("POST", "/api/businesses/" + businessId + "/services", """
				{"name": "Saç kesimi", "durationMinutes": 30, "priceCents": 50000}
				"""));
		String staffId = id(send("POST", "/api/businesses/" + businessId + "/staff", """
				{"displayName": "Ayşe"}
				"""));
		send("PUT", "/api/businesses/" + businessId + "/staff/" + staffId + "/services", """
				{"serviceIds": ["%s"]}
				""".formatted(serviceId));
		send("PUT", "/api/businesses/" + businessId + "/staff/" + staffId + "/working-hours",
				BookingApiTests.everyDay("09:00", "18:00"));

		String startAt = BookingApiTests.DAY.atTime(LocalTime.parse("14:00")).atZone(BookingApiTests.ISTANBUL)
				.toOffsetDateTime().toString();

		// All threads wait at the gate and are released together to maximise contention.
		CountDownLatch gate = new CountDownLatch(1);
		List<Future<Integer>> statuses = new ArrayList<>();
		try (ExecutorService pool = Executors.newFixedThreadPool(CONCURRENT_REQUESTS)) {
			for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
				String body = """
						{"staffId": "%s", "serviceId": "%s", "customerId": "%s", "startAt": "%s"}
						""".formatted(staffId, serviceId, UUID.randomUUID(), startAt);
				statuses.add(pool.submit(() -> {
					gate.await();
					return send("POST", "/api/businesses/" + businessId + "/appointments", body).statusCode();
				}));
			}
			gate.countDown();

			List<Integer> results = new ArrayList<>();
			for (Future<Integer> status : statuses) {
				results.add(status.get());
			}

			assertThat(results).filteredOn(s -> s == 201).hasSize(1);
			assertThat(results).filteredOn(s -> s == 409).hasSize(CONCURRENT_REQUESTS - 1);
		}

		Integer booked = jdbc.sql("SELECT count(*) FROM appointment WHERE staff_id = ?::uuid AND status = 'BOOKED'")
				.param(staffId)
				.query(Integer.class)
				.single();
		assertThat(booked).isEqualTo(1);
	}

	private String createBusiness() throws Exception {
		String slug = "concurrency-" + UUID.randomUUID().toString().substring(0, 8);
		return id(send("POST", "/api/businesses", """
				{"name": "Test", "slug": "%s", "timezone": "Europe/Istanbul"}
				""".formatted(slug)));
	}

	private HttpResponse<String> send(String method, String path, String json) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
				.header("Content-Type", "application/json")
				.method(method, HttpRequest.BodyPublishers.ofString(json))
				.build();
		return http.send(request, HttpResponse.BodyHandlers.ofString());
	}

	private static String id(HttpResponse<String> response) {
		String location = response.headers().firstValue("Location").orElseThrow();
		return location.substring(location.lastIndexOf('/') + 1);
	}

}
