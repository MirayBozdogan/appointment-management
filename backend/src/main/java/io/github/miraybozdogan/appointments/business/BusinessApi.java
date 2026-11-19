package io.github.miraybozdogan.appointments.business;

import java.util.UUID;

/**
 * Public API of the business module, used by other modules.
 */
public interface BusinessApi {

	/**
	 * @throws BusinessNotFoundException if no business has this id
	 */
	BusinessView get(UUID id);

}
