# Backend DDD Standard

## Package Shape

Use this direction inside each backend service:

```text
com.symphonyforgeops.{service}
  interfaces.web        # controllers, request/response adapters
  application.service   # use cases, transactions, orchestration of ports
  application.dto       # API/application DTOs
  domain.model          # business records/enums/value objects
  domain.repository     # repository interfaces when useful
  infrastructure.*      # JDBC/MyBatis, GitHub, filesystem, process adapters
```

## Dependency Rule

- `domain` must not depend on Spring, JDBC, HTTP clients, filesystem APIs, or agent SDKs.
- `application` may depend on domain and ports.
- `interfaces` maps HTTP to application calls.
- `infrastructure` implements persistence, filesystem, GitHub, command, and agent ports.

## API Rule

- REST path prefix is `/api/v1/{domain}`.
- Responses use `ApiResponse<T>`.
- Errors use stable codes once a domain is implemented.
- Validate request shape at the boundary. Do not let raw maps leak through application code except for intentionally schema-like fields.

## Persistence Rule

- Flyway SQL is authoritative for DDL.
- Migration naming: `VYYYYMMDD_NNN__description.sql`.
- Add tables by stage; do not create all future tables at once.
- Prefer clear SQL and small repositories over generic abstraction.
- MySQL stores control-plane state and logs, not managed repository source code.

## Testing Rule

- New behavior starts with a failing test.
- Controller/API behavior should have MockMvc integration tests.
- Parser/path-safety logic should have focused unit tests.
- Migration must be verified against an empty MySQL schema before a stage is marked done.
