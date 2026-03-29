package com.github.dimitryivaniuta.gateway.realestate.security;

import java.net.InetSocketAddress;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Resolves the effective client IP address.
 */
@Component
public class IpAddressResolver {

    /**
     * Resolves the client IP using the first X-Forwarded-For value when present.
     *
     * @param request inbound request
     * @return source ip text
     */
    public String resolve(final ServerHttpRequest request) {
        final String forwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        final InetSocketAddress remoteAddress = request.getRemoteAddress();
        return remoteAddress == null || remoteAddress.getAddress() == null
                ? "unknown"
                : remoteAddress.getAddress().getHostAddress();
    }
}
