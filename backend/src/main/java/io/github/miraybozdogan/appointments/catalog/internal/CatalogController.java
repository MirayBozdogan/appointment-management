package io.github.miraybozdogan.appointments.catalog.internal;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.miraybozdogan.appointments.catalog.ServiceView;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/businesses/{businessId}/services")
class CatalogController {

	private final CatalogService catalog;

	CatalogController(CatalogService catalog) {
		this.catalog = catalog;
	}

	@GetMapping
	List<ServiceView> list(@PathVariable UUID businessId) {
		return catalog.list(businessId);
	}

	@GetMapping("/{serviceId}")
	ServiceView get(@PathVariable UUID businessId, @PathVariable UUID serviceId) {
		return catalog.get(businessId, serviceId);
	}

	@PostMapping
	ResponseEntity<ServiceView> create(@PathVariable UUID businessId, @Valid @RequestBody ServiceRequest request) {
		ServiceView created = catalog.create(businessId, request);
		URI location = URI.create("/api/businesses/" + businessId + "/services/" + created.id());
		return ResponseEntity.created(location).body(created);
	}

	@PutMapping("/{serviceId}")
	ServiceView update(@PathVariable UUID businessId, @PathVariable UUID serviceId,
			@Valid @RequestBody ServiceRequest request) {
		return catalog.update(businessId, serviceId, request);
	}

}
