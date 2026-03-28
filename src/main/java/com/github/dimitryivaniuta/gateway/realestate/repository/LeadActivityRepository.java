package com.github.dimitryivaniuta.gateway.realestate.repository;

import com.github.dimitryivaniuta.gateway.realestate.domain.LeadActivity;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

/**
 * Reactive repository for lead activity history.
 */
public interface LeadActivityRepository extends ReactiveCrudRepository<LeadActivity, UUID> {

    /**
     * Returns all activities for one lead ordered from newest to oldest.
     *
     * @param leadId lead identifier
     * @return audit history stream
     */
    Flux<LeadActivity> findAllByLeadIdOrderByOccurredAtDesc(UUID leadId);
}
