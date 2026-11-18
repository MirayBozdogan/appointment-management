package io.github.miraybozdogan.appointments;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Verifies the module boundaries described in docs/architecture.md:
 * modules only use each other's public API and there are no dependency cycles.
 */
class ModularityTests {

	private final ApplicationModules modules = ApplicationModules.of(AppointmentsApplication.class);

	@Test
	void modulesRespectTheirBoundaries() {
		modules.verify();
	}

	@Test
	void writeModuleDocumentation() {
		new Documenter(modules).writeDocumentation();
	}
}
