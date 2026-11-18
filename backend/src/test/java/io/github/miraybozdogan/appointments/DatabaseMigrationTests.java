package io.github.miraybozdogan.appointments;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class DatabaseMigrationTests {

	@Autowired
	JdbcClient jdbc;

	@Test
	void flywayEnablesBtreeGist() {
		boolean installed = jdbc.sql("SELECT EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'btree_gist')")
				.query(Boolean.class)
				.single();

		assertThat(installed).isTrue();
	}

}
