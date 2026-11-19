package io.github.miraybozdogan.appointments.business.internal;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.miraybozdogan.appointments.business.BusinessView;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/businesses")
class BusinessController {

	private final BusinessService service;

	BusinessController(BusinessService service) {
		this.service = service;
	}

	@PostMapping
	ResponseEntity<BusinessView> create(@Valid @RequestBody CreateBusinessRequest request) {
		BusinessView created = service.create(request);
		return ResponseEntity.created(URI.create("/api/businesses/" + created.id())).body(created);
	}

	@GetMapping("/{id}")
	BusinessView get(@PathVariable UUID id) {
		return service.get(id);
	}

}
