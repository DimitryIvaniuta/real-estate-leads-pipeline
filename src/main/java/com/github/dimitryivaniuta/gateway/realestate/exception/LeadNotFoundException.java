package com.github.dimitryivaniuta.gateway.realestate.exception;

import java.util.UUID;

/**
 * Raised when a lead cannot be found.
 */
public class LeadNotFoundException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param id lead identifier
     */
    public LeadNotFoundException(final UUID id) {
        super("Lead not found: " + id);
    }
}
