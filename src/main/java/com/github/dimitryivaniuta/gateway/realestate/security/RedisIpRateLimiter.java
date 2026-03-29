package com.github.dimitryivaniuta.gateway.realestate.security;

import com.github.dimitryivaniuta.gateway.realestate.config.AppProperties;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Minimal per-IP fixed-window limiter backed by Redis.
 */
@Component
@RequiredArgsConstructor
public class RedisIpRateLimiter {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final AppProperties appProperties;

    /**
     * Consumes one request slot for the given IP.
     *
     * @param ip source ip
     * @return true when request is allowed
     */
    public Mono<Boolean> allow(final String ip) {
        final String key = "security:rate:" + ip;
        final Duration window = Duration.ofMinutes(1);
        return redisTemplate.opsForValue().increment(key)
                .flatMap(count -> {
                    final Mono<Boolean> result = Mono.just(count <= appProperties.rateLimit().burstCapacity());
                    if (count == 1) {
                        return redisTemplate.expire(key, window).then(result);
                    }
                    return result;
                });
    }
}
