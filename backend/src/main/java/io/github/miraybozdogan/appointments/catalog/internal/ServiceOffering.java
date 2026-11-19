package io.github.miraybozdogan.appointments.catalog.internal;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import io.github.miraybozdogan.appointments.catalog.ServiceView;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A service a business offers. Named ServiceOffering to avoid confusion with Spring's
 * {@code @Service} annotation; the table is called {@code service}.
 */
@Entity
@Table(name = "service")
class ServiceOffering {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	private UUID id;

	@Column(name = "business_id", updatable = false)
	private UUID businessId;

	private String name;

	@Column(name = "duration_minutes")
	private int durationMinutes;

	@Column(name = "price_cents")
	private int priceCents;

	private boolean active;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private Instant createdAt;

	protected ServiceOffering() {
		// for JPA
	}

	ServiceOffering(UUID businessId, String name, int durationMinutes, int priceCents) {
		this.businessId = businessId;
		this.name = name;
		this.durationMinutes = durationMinutes;
		this.priceCents = priceCents;
		this.active = true;
	}

	void update(String name, int durationMinutes, int priceCents, boolean active) {
		this.name = name;
		this.durationMinutes = durationMinutes;
		this.priceCents = priceCents;
		this.active = active;
	}

	ServiceView toView() {
		return new ServiceView(id, businessId, name, durationMinutes, priceCents, active);
	}

}
