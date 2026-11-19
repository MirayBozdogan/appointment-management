package io.github.miraybozdogan.appointments.staff.internal;

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

import io.github.miraybozdogan.appointments.staff.StaffView;
import io.github.miraybozdogan.appointments.staff.WorkingInterval;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/businesses/{businessId}/staff")
class StaffController {

	private final StaffService service;

	StaffController(StaffService service) {
		this.service = service;
	}

	@GetMapping
	List<StaffView> list(@PathVariable UUID businessId) {
		return service.list(businessId);
	}

	@GetMapping("/{staffId}")
	StaffView get(@PathVariable UUID businessId, @PathVariable UUID staffId) {
		return service.get(businessId, staffId);
	}

	@PostMapping
	ResponseEntity<StaffView> create(@PathVariable UUID businessId, @Valid @RequestBody CreateStaffRequest request) {
		StaffView created = service.create(businessId, request);
		URI location = URI.create("/api/businesses/" + businessId + "/staff/" + created.id());
		return ResponseEntity.created(location).body(created);
	}

	@PutMapping("/{staffId}/services")
	StaffView assignServices(@PathVariable UUID businessId, @PathVariable UUID staffId,
			@Valid @RequestBody AssignServicesRequest request) {
		return service.assignServices(businessId, staffId, request);
	}

	@GetMapping("/{staffId}/working-hours")
	List<WorkingInterval> workingHours(@PathVariable UUID businessId, @PathVariable UUID staffId) {
		return service.workingHours(businessId, staffId);
	}

	@PutMapping("/{staffId}/working-hours")
	List<WorkingInterval> replaceWorkingHours(@PathVariable UUID businessId, @PathVariable UUID staffId,
			@Valid @RequestBody WorkingHoursRequest request) {
		return service.replaceWorkingHours(businessId, staffId, request);
	}

}
