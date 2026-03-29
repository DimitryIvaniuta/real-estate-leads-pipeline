package com.github.dimitryivaniuta.gateway.realestate.service;

import com.github.dimitryivaniuta.gateway.realestate.domain.LeadPhase;
import com.github.dimitryivaniuta.gateway.realestate.dto.LeadPipelineSummaryResponse;
import com.github.dimitryivaniuta.gateway.realestate.repository.LeadRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Builds tenant pipeline summaries.
 */
@Service
@RequiredArgsConstructor
public class LeadSummaryService {

    private final LeadRepository leadRepository;

    /**
     * Returns counts per lead phase for one tenant.
     *
     * @param tenantId tenant identifier
     * @return phase totals response
     */
    public Mono<LeadPipelineSummaryResponse> summarize(final UUID tenantId) {
        return Mono.zip(
                        leadRepository.countByTenantIdAndPhase(tenantId, LeadPhase.DISCOVERY),
                        leadRepository.countByTenantIdAndPhase(tenantId, LeadPhase.QUALIFIED),
                        leadRepository.countByTenantIdAndPhase(tenantId, LeadPhase.OPPORTUNITY),
                        leadRepository.countByTenantIdAndPhase(tenantId, LeadPhase.DEAL))
                .map(tuple -> {
                    final Map<String, Long> totals = new LinkedHashMap<>();
                    totals.put(LeadPhase.DISCOVERY.name(), tuple.getT1());
                    totals.put(LeadPhase.QUALIFIED.name(), tuple.getT2());
                    totals.put(LeadPhase.OPPORTUNITY.name(), tuple.getT3());
                    totals.put(LeadPhase.DEAL.name(), tuple.getT4());
                    return new LeadPipelineSummaryResponse(totals);
                });
    }
}
