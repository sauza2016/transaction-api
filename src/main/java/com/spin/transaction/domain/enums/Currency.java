package com.spin.transaction.domain.enums;

/**
 * Supported currencies for transaction execution.
 *
 * <p>
 * Currently, only MXN is supported according to the business requirements.
 * The enum allows future extension without modifying the domain model.
 * </p>
 */
public enum Currency {
    /**
     * Mexican Peso.
     */
	MXN
}
