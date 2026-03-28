package com.github.dimitryivaniuta.gateway.realestate.repository;

import com.github.dimitryivaniuta.gateway.realestate.domain.Lead;
import com.github.dimitryivaniuta.gateway.realestate.domain.LeadPhase;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive repository for leads.
 */
public interface LeadRepository extends ReactiveCrudRepository<Lead, UUID> {

    /**
     * Finds all leads for a tenant.
     *
     * @param tenantId tenant identifier
     * @return matching leads
     */
    Flux<Lead> findAllByTenantId(UUID tenantId);

    /**
     * Finds one lead by id and tenant.
     *
     * @param id lead identifier
     * @param tenantId tenant identifier
     * @return matching lead when present
     */
    Mono<Lead> findByIdAndTenantId(UUID id, UUID tenantId);

    /**
     * Counts tenant leads in one phase.
     *
     * @param tenantId tenant identifier
     * @param phase pipeline phase
     * @return count
     */
    Mono<Long> countByTenantIdAndPhase(UUID tenantId, LeadPhase phase);
}
