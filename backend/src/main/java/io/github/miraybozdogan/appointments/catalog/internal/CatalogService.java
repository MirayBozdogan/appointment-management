package io.github.miraybozdogan.appointments.catalog.internal;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.miraybozdogan.appointments.business.BusinessApi;
import io.github.miraybozdogan.appointments.catalog.CatalogApi;
import io.github.miraybozdogan.appointments.catalog.ServiceNotFoundException;
import io.github.miraybozdogan.appointments.catalog.ServiceView;

@Service
@Transactional(readOnly = true)
class CatalogService implements CatalogApi {

	private final ServiceOfferingRepository services;

	private final BusinessApi businesses;

	CatalogService(ServiceOfferingRepository services, BusinessApi businesses) {
		this.services = services;
		this.businesses = businesses;
	}

	@Override
	public ServiceView get(UUID businessId, UUID serviceId) {
		return find(businessId, serviceId).toView();
	}

	List<ServiceView> list(UUID businessId) {
		businesses.get(businessId); // 404 if the business does not exist
		return services.findByBusinessIdOrderByName(businessId).stream()
				.map(ServiceOffering::toView)
				.toList();
	}

	@Transactional
	ServiceView create(UUID businessId, ServiceRequest request) {
		businesses.get(businessId);
		String name = request.name().trim();
		if (services.existsByBusinessIdAndNameIgnoreCase(businessId, name)) {
			throw new ServiceNameTakenException(name);
		}
		ServiceOffering service = new ServiceOffering(businessId, name, request.durationMinutes(),
				request.priceCents());
		return services.save(service).toView();
	}

	@Transactional
	ServiceView update(UUID businessId, UUID serviceId, ServiceRequest request) {
		ServiceOffering service = find(businessId, serviceId);
		String name = request.name().trim();
		if (services.existsByBusinessIdAndNameIgnoreCaseAndIdNot(businessId, name, serviceId)) {
			throw new ServiceNameTakenException(name);
		}
		boolean active = request.active() == null || request.active();
		service.update(name, request.durationMinutes(), request.priceCents(), active);
		return service.toView();
	}

	private ServiceOffering find(UUID businessId, UUID serviceId) {
		return services.findByIdAndBusinessId(serviceId, businessId)
				.orElseThrow(() -> new ServiceNotFoundException(serviceId));
	}

}
