package com.github.dimitryivaniuta.gateway.realestate.exception;

import com.github.dimitryivaniuta.gateway.realestate.domain.LeadPhase;

/**
 * Raised when a lead phase change violates pipeline rules.
 */
public class InvalidLeadPhaseTransitionException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param current current phase
     * @param target target phase
     */
    public InvalidLeadPhaseTransitionException(final LeadPhase current, final LeadPhase target) {
        super("Invalid lead phase transition from " + current + " to " + target);
    }
}
