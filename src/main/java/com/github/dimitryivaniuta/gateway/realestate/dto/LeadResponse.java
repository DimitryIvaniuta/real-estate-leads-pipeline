package com.github.dimitryivaniuta.gateway.realestate.dto;

import com.github.dimitryivaniuta.gateway.realestate.domain.Lead;
import com.github.dimitryivaniuta.gateway.realestate.domain.LeadPhase;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * API response for a lead.
 */
public record LeadResponse(
        UUID id,
        UUID tenantId,
        String agentId,
        String clientName,
        String clientEmail,
        String clientPhone,
        String propertyArea,
        BigDecimal budgetAmount,
        LeadPhase phase,
        String sourceChannel,
        Instant createdAt,
        Instant updatedAt,
        String lastModifiedBy) {

    /**
     * Maps a domain aggregate to a response.
     *
     * @param lead source aggregate
     * @return api response
     */
    public static LeadResponse from(final Lead lead) {
        return new LeadResponse(
                lead.getId(),
                lead.getTenantId(),
                lead.getAgentId(),
                lead.getClientName(),
                lead.getClientEmail(),
                lead.getClientPhone(),
                lead.getPropertyArea(),
                lead.getBudgetAmount(),
                lead.getPhase(),
                lead.getSourceChannel(),
                lead.getCreatedAt(),
                lead.getUpdatedAt(),
                lead.getLastModifiedBy());
    }
}
