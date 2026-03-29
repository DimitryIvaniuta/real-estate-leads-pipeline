package com.github.dimitryivaniuta.gateway.realestate.service;

import com.github.dimitryivaniuta.gateway.realestate.domain.LeadPhase;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Encapsulates allowed forward-only lead phase transitions.
 */
@Component
public class LeadPhasePolicy {

    private static final Map<LeadPhase, Set<LeadPhase>> ALLOWED_TRANSITIONS = Map.of(
            LeadPhase.DISCOVERY, Set.of(LeadPhase.QUALIFIED),
            LeadPhase.QUALIFIED, Set.of(LeadPhase.OPPORTUNITY),
            LeadPhase.OPPORTUNITY, Set.of(LeadPhase.DEAL),
            LeadPhase.DEAL, Set.of());

    /**
     * Returns whether the transition is allowed.
     *
     * @param current current phase
     * @param target target phase
     * @return true when allowed
     */
    public boolean isAllowed(final LeadPhase current, final LeadPhase target) {
        return ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(target);
    }
}
