package com.github.dimitryivaniuta.gateway.realestate.security;

import com.github.dimitryivaniuta.gateway.realestate.config.AppProperties;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Validates browser Origin or Referer for state-changing portal requests as defense in depth on top of CSRF tokens.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class PortalOriginValidationFilter implements WebFilter {

    private final AppProperties appProperties;

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final WebFilterChain chain) {
        final String path = exchange.getRequest().getPath().pathWithinApplication().value();
        final HttpMethod method = exchange.getRequest().getMethod();
        if (!path.startsWith("/portal/") || method == null || isSafeMethod(method)) {
            return chain.filter(exchange);
        }

        final String origin = exchange.getRequest().getHeaders().getFirst("Origin");
        final String referer = exchange.getRequest().getHeaders().getFirst("Referer");
        if (isAllowedOrigin(origin) || isAllowedReferer(referer)) {
            return chain.filter(exchange);
        }
        return reject(exchange, "Blocked by portal origin policy");
    }

    private boolean isSafeMethod(final HttpMethod method) {
        return HttpMethod.GET.equals(method) || HttpMethod.HEAD.equals(method) || HttpMethod.OPTIONS.equals(method);
    }

    private boolean isAllowedOrigin(final String origin) {
        if (!StringUtils.hasText(origin)) {
            return false;
        }
        return appProperties.portal().allowedOrigins().contains(origin);
    }

    private boolean isAllowedReferer(final String referer) {
        if (!StringUtils.hasText(referer)) {
            return false;
        }
        return appProperties.portal().allowedOrigins().stream().anyMatch(allowedOrigin -> {
            try {
                final URI refererUri = URI.create(referer);
                final String candidate = refererUri.getScheme() + "://" + refererUri.getAuthority();
                return allowedOrigin.equals(candidate);
            } catch (IllegalArgumentException ex) {
                return false;
            }
        });
    }

    private Mono<Void> reject(final ServerWebExchange exchange, final String message) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        final byte[] body = ("{\"message\":\"" + message + "\"}").getBytes();
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
    }
}
