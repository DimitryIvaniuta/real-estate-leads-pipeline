package com.github.dimitryivaniuta.gateway.realestate.security;

import com.github.dimitryivaniuta.gateway.realestate.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Blocks known hostile paths and abusive source IPs before application logic runs.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class SuspiciousRequestFilter implements WebFilter {

    private final AppProperties appProperties;
    private final IpAddressResolver ipAddressResolver;
    private final IpThreatProtectionService ipThreatProtectionService;
    private final RedisIpRateLimiter redisIpRateLimiter;

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final WebFilterChain chain) {
        final String path = exchange.getRequest().getPath().pathWithinApplication().value();
        final String ip = ipAddressResolver.resolve(exchange.getRequest());

        return ipThreatProtectionService.isBlocked(ip)
                .flatMap(blocked -> blocked
                        ? reject(exchange, HttpStatus.FORBIDDEN, "IP temporarily blocked")
                        : handleAllowedIp(exchange, chain, path, ip));
    }

    private Mono<Void> handleAllowedIp(final ServerWebExchange exchange,
                                       final WebFilterChain chain,
                                       final String path,
                                       final String ip) {
        if (isSuspiciousPath(path)) {
            return ipThreatProtectionService.recordSuspiciousActivity(ip, path, "blocked-known-hostile-path")
                    .then(reject(exchange, HttpStatus.FORBIDDEN, "Blocked suspicious path"));
        }
        return redisIpRateLimiter.allow(ip)
                .flatMap(allowed -> allowed
                        ? chain.filter(exchange)
                        : ipThreatProtectionService.recordSuspiciousActivity(ip, path, "rate-limit-exceeded")
                                .then(reject(exchange, HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded")));
    }

    private boolean isSuspiciousPath(final String path) {
        return appProperties.security().blockedPathPrefixes().stream().anyMatch(path::startsWith);
    }

    private Mono<Void> reject(final ServerWebExchange exchange, final HttpStatus status, final String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        final byte[] body = ("{\"message\":\"" + message + "\"}").getBytes();
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                .bufferFactory()
                .wrap(body)));
    }
}
