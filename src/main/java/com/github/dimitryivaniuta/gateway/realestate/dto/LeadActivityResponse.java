package com.github.dimitryivaniuta.gateway.realestate.dto;

import com.github.dimitryivaniuta.gateway.realestate.domain.LeadActionType;
import com.github.dimitryivaniuta.gateway.realestate.domain.LeadActivity;
import java.time.Instant;
import java.util.UUID;

/**
 * API response for lead audit history entries.
 */
public record LeadActivityResponse(
        UUID id,
        UUID leadId,
        UUID tenantId,
        LeadActionType actionType,
        String fromPhase,
        String toPhase,
        String actor,
        Instant occurredAt,
        String details) {

    /**
     * Maps an activity entity to an API response.
     *
     * @param activity source activity
     * @return mapped response
     */
    public static LeadActivityResponse from(final LeadActivity activity) {
        return new LeadActivityResponse(
                activity.getId(),
                activity.getLeadId(),
                activity.getTenantId(),
                activity.getActionType(),
                activity.getFromPhase(),
                activity.getToPhase(),
                activity.getActor(),
                activity.getOccurredAt(),
                activity.getDetails());
    }
}
