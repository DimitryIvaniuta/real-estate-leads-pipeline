package com.github.dimitryivaniuta.gateway.realestate.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.dimitryivaniuta.gateway.realestate.config.AppProperties;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for portal origin validation.
 */
class PortalOriginValidationFilterTest {

    private final PortalOriginValidationFilter filter = new PortalOriginValidationFilter(
            new AppProperties(
                    new AppProperties.Security(List.of("/xmlrpc.php"), Duration.ofMinutes(30), 3),
                    new AppProperties.RateLimit(60, 120),
                    new AppProperties.Portal(List.of("https://portal.example.com")),
                    new AppProperties.Topics("lead.phase.changed", "security.threat.detected")));

    @Test
    void shouldAllowSafeGetWithoutOriginCheck() {
        final MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/portal/csrf"));
        final WebFilterChain chain = ignored -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void shouldBlockPortalWriteWhenOriginMissing() {
        final MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/portal/form"));
        final WebFilterChain chain = ignored -> Mono.error(new IllegalStateException("should not continue"));

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldAllowPortalWriteWhenOriginMatches() {
        final MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/portal/form").header("Origin", "https://portal.example.com"));
        final WebFilterChain chain = ignored -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
    }
}
