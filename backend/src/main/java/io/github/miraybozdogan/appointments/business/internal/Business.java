package io.github.miraybozdogan.appointments.business.internal;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import io.github.miraybozdogan.appointments.business.BusinessView;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "business")
class Business {

	static final int DEFAULT_OFFER_TTL_MINUTES = 15;

	@Id
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	private UUID id;

	private String name;

	private String slug;

	private String timezone;

	@Column(name = "offer_ttl_minutes")
	private int offerTtlMinutes;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private Instant createdAt;

	protected Business() {
		// for JPA
	}

	Business(String name, String slug, ZoneId timezone, int offerTtlMinutes) {
		this.name = name;
		this.slug = slug;
		this.timezone = timezone.getId();
		this.offerTtlMinutes = offerTtlMinutes;
	}

	BusinessView toView() {
		return new BusinessView(id, name, slug, ZoneId.of(timezone), offerTtlMinutes);
	}

}
