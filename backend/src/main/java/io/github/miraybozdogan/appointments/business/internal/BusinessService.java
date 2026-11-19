package io.github.miraybozdogan.appointments.business.internal;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.miraybozdogan.appointments.business.BusinessApi;
import io.github.miraybozdogan.appointments.business.BusinessNotFoundException;
import io.github.miraybozdogan.appointments.business.BusinessView;

@Service
@Transactional(readOnly = true)
class BusinessService implements BusinessApi {

	private final BusinessRepository businesses;

	BusinessService(BusinessRepository businesses) {
		this.businesses = businesses;
	}

	@Override
	public BusinessView get(UUID id) {
		return businesses.findById(id)
				.map(Business::toView)
				.orElseThrow(() -> new BusinessNotFoundException(id));
	}

	@Transactional
	BusinessView create(CreateBusinessRequest request) {
		ZoneId timezone = parseTimezone(request.timezone());
		if (businesses.existsBySlug(request.slug())) {
			throw new SlugAlreadyTakenException(request.slug());
		}
		int offerTtl = request.offerTtlMinutes() != null
				? request.offerTtlMinutes()
				: Business.DEFAULT_OFFER_TTL_MINUTES;

		Business business = new Business(request.name().trim(), request.slug(), timezone, offerTtl);
		return businesses.save(business).toView();
	}

	private static ZoneId parseTimezone(String value) {
		try {
			return ZoneId.of(value);
		}
		catch (DateTimeException e) {
			throw new InvalidTimezoneException(value);
		}
	}

}
