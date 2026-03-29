package com.github.dimitryivaniuta.gateway.realestate.controller;

import java.util.Map;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Exposes the CSRF token for browser-based portal clients.
 */
@RestController
@RequestMapping("/portal")
public class PortalSecurityController {

    /**
     * Returns the reactive CSRF token value and header name.
     *
     * @param csrfToken token request attribute
     * @return token payload
     */
    @GetMapping("/csrf")
    public Mono<Map<String, String>> csrf(@RequestAttribute final Mono<CsrfToken> csrfToken) {
        return csrfToken.map(token -> Map.of(
                "headerName", token.getHeaderName(),
                "parameterName", token.getParameterName(),
                "token", token.getToken()));
    }
}
