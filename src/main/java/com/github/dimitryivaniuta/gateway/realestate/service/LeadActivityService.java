package com.github.dimitryivaniuta.gateway.realestate.service;

import com.github.dimitryivaniuta.gateway.realestate.domain.Lead;
import com.github.dimitryivaniuta.gateway.realestate.domain.LeadActionType;
import com.github.dimitryivaniuta.gateway.realestate.domain.LeadActivity;
import com.github.dimitryivaniuta.gateway.realestate.domain.LeadPhase;
import com.github.dimitryivaniuta.gateway.realestate.repository.LeadActivityRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Persists and exposes lead audit history.
 */
@Service
@RequiredArgsConstructor
public class LeadActivityService {

    private final LeadActivityRepository leadActivityRepository;

    /**
     * Stores a creation audit record.
     *
     * @param lead created lead
     * @param actor actor login
     * @return completion signal
     */
    public Mono<Void> recordCreated(final Lead lead, final String actor) {
        return save(lead, LeadActionType.CREATED, null, lead.getPhase(), actor,
                "Lead created from source channel " + lead.getSourceChannel());
    }

    /**
     * Stores a phase-change audit record.
     *
     * @param lead changed lead
     * @param fromPhase previous phase
     * @param toPhase new phase
     * @param actor actor login
     * @return completion signal
     */
    public Mono<Void> recordPhaseChanged(final Lead lead,
                                         final LeadPhase fromPhase,
                                         final LeadPhase toPhase,
                                         final String actor) {
        return save(lead, LeadActionType.PHASE_CHANGED, fromPhase, toPhase, actor,
                "Lead phase moved by workflow policy");
    }

    /**
     * Returns activity history for one lead.
     *
     * @param leadId lead identifier
     * @return audit history stream
     */
    public Flux<LeadActivity> listByLeadId(final UUID leadId) {
        return leadActivityRepository.findAllByLeadIdOrderByOccurredAtDesc(leadId);
    }

    private Mono<Void> save(final Lead lead,
                            final LeadActionType actionType,
                            final LeadPhase fromPhase,
                            final LeadPhase toPhase,
                            final String actor,
                            final String details) {
        final LeadActivity activity = LeadActivity.builder()
                .id(UUID.randomUUID())
                .leadId(lead.getId())
                .tenantId(lead.getTenantId())
                .actionType(actionType)
                .fromPhase(fromPhase == null ? null : fromPhase.name())
                .toPhase(toPhase == null ? null : toPhase.name())
                .actor(actor)
                .occurredAt(Instant.now())
                .details(details)
                .build();
        return leadActivityRepository.save(activity).then();
    }
}
