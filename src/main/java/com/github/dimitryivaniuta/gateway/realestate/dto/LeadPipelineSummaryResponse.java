package com.github.dimitryivaniuta.gateway.realestate.dto;

import java.util.Map;

/**
 * API response containing lead counts by phase for one tenant.
 */
public record LeadPipelineSummaryResponse(Map<String, Long> totalsByPhase) {
}
