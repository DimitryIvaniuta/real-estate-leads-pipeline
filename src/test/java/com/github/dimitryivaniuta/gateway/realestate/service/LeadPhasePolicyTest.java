package com.github.dimitryivaniuta.gateway.realestate.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.dimitryivaniuta.gateway.realestate.domain.LeadPhase;
import org.junit.jupiter.api.Test;

/**
 * Tests for phase transition policy.
 */
class LeadPhasePolicyTest {

    private final LeadPhasePolicy policy = new LeadPhasePolicy();

    @Test
    void shouldAllowForwardTransitionsOnly() {
        assertThat(policy.isAllowed(LeadPhase.DISCOVERY, LeadPhase.QUALIFIED)).isTrue();
        assertThat(policy.isAllowed(LeadPhase.QUALIFIED, LeadPhase.OPPORTUNITY)).isTrue();
        assertThat(policy.isAllowed(LeadPhase.OPPORTUNITY, LeadPhase.DEAL)).isTrue();
        assertThat(policy.isAllowed(LeadPhase.DISCOVERY, LeadPhase.DEAL)).isFalse();
        assertThat(policy.isAllowed(LeadPhase.DEAL, LeadPhase.OPPORTUNITY)).isFalse();
    }
}
