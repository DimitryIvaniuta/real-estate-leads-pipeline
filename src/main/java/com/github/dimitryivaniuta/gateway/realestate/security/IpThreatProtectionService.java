package com.github.dimitryivaniuta.gateway.realestate.security;

import com.github.dimitryivaniuta.gateway.realestate.config.AppProperties;
import com.github.dimitryivaniuta.gateway.realestate.service.LeadEventsPublisher;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Tracks suspicious IP activity and performs short-term denylisting.
 */
@Service
@RequiredArgsConstructor
public class IpThreatProtectionService {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final AppProperties appProperties;
    private final LeadEventsPublisher leadEventsPublisher;
    private final MeterRegistry meterRegistry;

    /**
     * Returns whether the ip is currently denylisted.
     *
     * @param ip source ip
     * @return true when denylisted
     */
    public Mono<Boolean> isBlocked(final String ip) {
        return redisTemplate.hasKey(blockedKey(ip)).defaultIfEmpty(false);
    }

    /**
     * Records a suspicious event and deny-lists the source when the threshold is hit.
     *
     * @param ip source ip
     * @param path request path
     * @param reason reason text
     * @return completion signal
     */
    public Mono<Void> recordSuspiciousActivity(final String ip, final String path, final String reason) {
        return redisTemplate.opsForValue().increment(scoreKey(ip))
                .flatMap(score -> redisTemplate.expire(scoreKey(ip), appProperties.security().denylistTtl())
                        .then(handleScore(ip, path, reason, score)));
    }

    private Mono<Void> handleScore(final String ip, final String path, final String reason, final Long score) {
        Counter.builder("security.threat.events")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
        final Mono<Void> publish = leadEventsPublisher.publishSecurityThreat(path, ip, reason);
        if (score >= appProperties.security().blockThreshold()) {
            Counter.builder("security.threat.blocked")
                    .tag("reason", reason)
                    .register(meterRegistry)
                    .increment();
            return redisTemplate.opsForValue()
                    .set(blockedKey(ip), reason, appProperties.security().denylistTtl())
                    .then(publish);
        }
        return publish;
    }

    private String scoreKey(final String ip) {
        return "security:ip-score:" + ip;
    }

    private String blockedKey(final String ip) {
        return "security:blocked-ip:" + ip;
    }
}
