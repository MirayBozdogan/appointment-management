package io.github.miraybozdogan.appointments.shared.time;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Code that needs "now" takes a {@link Clock} instead of calling {@code Instant.now()},
 * so tests can control time (for example to expire waitlist offers).
 */
@Configuration(proxyBeanMethods = false)
class ClockConfiguration {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

}
