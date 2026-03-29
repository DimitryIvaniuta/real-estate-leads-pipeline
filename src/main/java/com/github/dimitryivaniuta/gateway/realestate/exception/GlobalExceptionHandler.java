package com.github.dimitryivaniuta.gateway.realestate.exception;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

/**
 * Centralized error mapping to RFC 9457 style problem details.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maps missing lead errors.
     *
     * @param ex error
     * @return problem response
     */
    @ExceptionHandler(LeadNotFoundException.class)
    public Mono<ProblemDetail> handleLeadNotFound(final LeadNotFoundException ex) {
        final ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create("https://example.internal/problems/lead-not-found"));
        problem.setTitle("Lead not found");
        return Mono.just(problem);
    }

    /**
     * Maps invalid phase transitions.
     *
     * @param ex error
     * @return problem response
     */
    @ExceptionHandler(InvalidLeadPhaseTransitionException.class)
    public Mono<ProblemDetail> handleInvalidTransition(final InvalidLeadPhaseTransitionException ex) {
        final ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setType(URI.create("https://example.internal/problems/invalid-phase-transition"));
        problem.setTitle("Invalid lead phase transition");
        return Mono.just(problem);
    }

    /**
     * Maps authorization errors.
     *
     * @param ex error
     * @return problem response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ProblemDetail> handleAccessDenied(final AccessDeniedException ex) {
        final ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problem.setType(URI.create("https://example.internal/problems/access-denied"));
        problem.setTitle("Access denied");
        return Mono.just(problem);
    }

    /**
     * Maps validation errors.
     *
     * @param ex error
     * @return problem response
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ProblemDetail> handleValidation(final WebExchangeBindException ex) {
        final ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setType(URI.create("https://example.internal/problems/validation-error"));
        problem.setTitle("Validation error");
        problem.setProperty("errors", ex.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList());
        return Mono.just(problem);
    }
}
