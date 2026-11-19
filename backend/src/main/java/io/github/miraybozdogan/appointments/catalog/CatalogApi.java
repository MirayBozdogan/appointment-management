package io.github.miraybozdogan.appointments.catalog;

import java.util.UUID;

/**
 * Public API of the catalog module, used by other modules.
 */
public interface CatalogApi {

	/**
	 * Returns a service only if it belongs to the given business.
	 *
	 * @throws ServiceNotFoundException if the service does not exist in this business
	 */
	ServiceView get(UUID businessId, UUID serviceId);

}
