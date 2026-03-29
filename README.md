# real-estate-leads-pipeline

Production-grade reactive Real Estate Agent–Client Leads pipeline built with Java 21, Spring Boot 4.0.5, WebFlux, R2DBC, PostgreSQL, Flyway, Redis, and Kafka.

## GitHub repository

- **Repository name:** `real-estate-leads-pipeline`
- **Description:** `Reactive real estate leads pipeline with tenant-aware authorization, lead audit history, CSRF-safe portal flows, Redis threat controls, Kafka events, and PostgreSQL persistence.`

## Production-grade improvements added

- Tenant-aware authorization checks using JWT `tenant_id` claim for agent-level requests
- Phase changes attribute the acting user from the authenticated JWT principal instead of trusting client-supplied actor data
- Lead activity audit history table and API endpoint
- Pipeline summary endpoint with per-phase totals
- Stronger stateless API security with `NoOpServerSecurityContextRepository`
- Defense-in-depth for browser portal with CSRF + Origin/Referer validation
- Externalized Kafka topic names
- Prometheus-friendly security counters for suspicious requests and blocked IPs
- Additional validation and RFC 9457 problem responses for access-denied and validation cases
- Jacoco test reporting configuration

## What is implemented

- Reactive lead pipeline with phases: `DISCOVERY -> QUALIFIED -> OPPORTUNITY -> DEAL`
- Reactive PostgreSQL access through Spring Data R2DBC
- Flyway migrations for `leads` and `lead_activities`
- Kafka publishing for lead phase changes and security threat events
- Redis-backed rate limiting and temporary IP denylisting
- Layered request protection for hostile probes such as `/xmlrpc.php`
- Separate security behavior for browser portal and stateless APIs
  - `/portal/**` uses session authentication plus CSRF tokens
  - `/api/**` uses JWT bearer tokens and disables CSRF because the API is stateless
- Problem Details error responses
- Actuator health, Flyway, and Prometheus endpoint exposure
- Postman collection for local testing
- Docker Compose dependencies for PostgreSQL, Redis, and Kafka KRaft

## Why blocking IPs is not enough

You can temporarily block abusive source IPs, and this project does that. But modern scanners rotate IPs, sit behind botnets, and hit many commodity paths. The professional approach is layered:

1. Block obviously hostile paths early.
2. Rate-limit abusive request patterns.
3. Keep a short denylist in Redis.
4. Use stateless bearer tokens for APIs.
5. Keep CSRF for browser/session flows.
6. Validate Origin or Referer for portal writes as extra protection.
7. Emit events for SIEM, dashboards, or automated edge controls.
8. Put the service behind a WAF, CDN, ingress controller, or reverse proxy.

## Sample endpoints

### Portal

- `GET /portal/csrf`

### Leads API

- `POST /api/v1/leads`
- `GET /api/v1/leads/{id}?tenantId={tenantId}`
- `GET /api/v1/leads?tenantId={tenantId}`
- `GET /api/v1/leads/summary?tenantId={tenantId}`
- `GET /api/v1/leads/{id}/activities?tenantId={tenantId}`
- `PUT /api/v1/leads/{id}/phase?tenantId={tenantId}`

## Local run prerequisites

- Java 21
- Gradle 8.14+
- Docker / Docker Compose

## Local infrastructure

```bash
docker compose up -d
```

## Application run

```bash
gradle bootRun
```

## Test run

```bash
gradle test jacocoTestReport
```

## JWT expectations

Example agent JWT claims:

```json
{
  "sub": "agent-1",
  "roles": ["AGENT"],
  "tenant_id": "11111111-1111-1111-1111-111111111111",
  "scope": "leads.read leads.write"
}
```

Managers can hold the `MANAGER` role and access any tenant.

## Deployment notes

- Keep `/api/**` behind OAuth2/OIDC JWT validation.
- Keep `/portal/**` for browser flows that need CSRF tokens and session login.
- Put reverse-proxy or WAF rules in front of this service for commodity attacks.
- Do not place application endpoints under Actuator health-group additional paths.
- Replace local secrets and URLs with environment-specific configuration.
