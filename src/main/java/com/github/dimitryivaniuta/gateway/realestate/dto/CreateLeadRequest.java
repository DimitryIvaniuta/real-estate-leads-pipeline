package com.github.dimitryivaniuta.gateway.realestate.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request payload for creating a lead.
 */
public record CreateLeadRequest(
        @NotNull UUID tenantId,
        @NotBlank String agentId,
        @NotBlank String clientName,
        @Email String clientEmail,
        @NotBlank @Pattern(regexp = "^[+0-9()\\- ]{7,32}$") String clientPhone,
        @NotBlank String propertyArea,
        @NotNull @DecimalMin("0.01") BigDecimal budgetAmount,
        @NotBlank String sourceChannel) {
}
