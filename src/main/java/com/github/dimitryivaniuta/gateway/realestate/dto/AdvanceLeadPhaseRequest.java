package com.github.dimitryivaniuta.gateway.realestate.dto;

import com.github.dimitryivaniuta.gateway.realestate.domain.LeadPhase;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for changing a lead phase.
 */
public record AdvanceLeadPhaseRequest(
        @NotNull LeadPhase targetPhase,
        @NotBlank String actor) {
}
