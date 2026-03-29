package com.github.dimitryivaniuta.gateway.realestate.controller;

import com.github.dimitryivaniuta.gateway.realestate.dto.AdvanceLeadPhaseRequest;
import com.github.dimitryivaniuta.gateway.realestate.dto.CreateLeadRequest;
import com.github.dimitryivaniuta.gateway.realestate.dto.LeadActivityResponse;
import com.github.dimitryivaniuta.gateway.realestate.dto.LeadPipelineSummaryResponse;
import com.github.dimitryivaniuta.gateway.realestate.dto.LeadResponse;
import com.github.dimitryivaniuta.gateway.realestate.security.TenantAccessService;
import com.github.dimitryivaniuta.gateway.realestate.service.LeadActivityService;
import com.github.dimitryivaniuta.gateway.realestate.service.LeadService;
import com.github.dimitryivaniuta.gateway.realestate.service.LeadSummaryService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive REST endpoints for lead management.
 */
@RestController
@RequestMapping("/api/v1/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;
    private final LeadActivityService leadActivityService;
    private final LeadSummaryService leadSummaryService;
    private final TenantAccessService tenantAccessService;

    /**
     * Creates a lead.
     *
     * @param request payload
     * @param authentication authenticated principal
     * @return created lead
     */
    @PostMapping
    public Mono<LeadResponse> createLead(@Valid @RequestBody final Mono<CreateLeadRequest> request,
                                         final JwtAuthenticationToken authentication) {
        return request.flatMap(body -> {
                    tenantAccessService.assertTenantAccess(authentication, body.tenantId());
                    final String actor = authentication.getName();
                    return leadService.createLead(body, actor);
                })
                .map(LeadResponse::from);
    }

    /**
     * Returns a lead by id.
     *
     * @param id lead identifier
     * @param tenantId tenant identifier
     * @param authentication authenticated principal
     * @return lead response
     */
    @GetMapping("/{id}")
    public Mono<LeadResponse> getLead(@PathVariable final UUID id,
                                      @RequestParam final UUID tenantId,
                                      final JwtAuthenticationToken authentication) {
        tenantAccessService.assertTenantAccess(authentication, tenantId);
        return leadService.getLead(id, tenantId).map(LeadResponse::from);
    }

    /**
     * Lists leads for a tenant.
     *
     * @param tenantId tenant id
     * @param authentication authenticated principal
     * @return lead stream
     */
    @GetMapping
    public Flux<LeadResponse> listByTenant(@RequestParam final UUID tenantId,
                                           final JwtAuthenticationToken authentication) {
        tenantAccessService.assertTenantAccess(authentication, tenantId);
        return leadService.listByTenant(tenantId).map(LeadResponse::from);
    }

    /**
     * Returns lead pipeline summary for a tenant.
     *
     * @param tenantId tenant id
     * @param authentication authenticated principal
     * @return summary response
     */
    @GetMapping("/summary")
    public Mono<LeadPipelineSummaryResponse> summary(@RequestParam final UUID tenantId,
                                                     final JwtAuthenticationToken authentication) {
        tenantAccessService.assertTenantAccess(authentication, tenantId);
        return leadSummaryService.summarize(tenantId);
    }

    /**
     * Returns lead audit activity history.
     *
     * @param id lead identifier
     * @param tenantId tenant identifier
     * @param authentication authenticated principal
     * @return activity stream
     */
    @GetMapping("/{id}/activities")
    public Flux<LeadActivityResponse> activities(@PathVariable final UUID id,
                                                 @RequestParam final UUID tenantId,
                                                 final JwtAuthenticationToken authentication) {
        tenantAccessService.assertTenantAccess(authentication, tenantId);
        return leadService.getLead(id, tenantId)
                .flatMapMany(lead -> leadActivityService.listByLeadId(lead.getId()))
                .map(LeadActivityResponse::from);
    }

    /**
     * Advances a lead phase.
     *
     * @param id lead identifier
     * @param tenantId tenant identifier
     * @param request payload
     * @param authentication authenticated principal
     * @return updated lead
     */
    @PutMapping("/{id}/phase")
    public Mono<LeadResponse> advanceLeadPhase(@PathVariable final UUID id,
                                               @RequestParam final UUID tenantId,
                                               @Valid @RequestBody final Mono<AdvanceLeadPhaseRequest> request,
                                               final JwtAuthenticationToken authentication) {
        tenantAccessService.assertTenantAccess(authentication, tenantId);
        return request.flatMap(body -> leadService.advanceLeadPhase(id, tenantId, body.targetPhase(), authentication.getName()))
                .map(LeadResponse::from);
    }
}
