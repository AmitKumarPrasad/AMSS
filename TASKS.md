# AMSS Implementation Tasks

This file is the implementation source of truth for the next development batches.

## Phase 1 - Foundation
- [x] Establish runnable Spring Boot backend.
- [x] Establish Python AI orchestrator.
- [x] Establish Docker/Docker Compose support.
- [x] Establish GitHub Actions CI.
- [x] Fix assessment source compilation issues.
- [x] Implement assessment status-change service method.
- [x] Confirm latest CI run is green.

## Phase 2 - Documentation and Quality
- [x] Add PRD.
- [x] Add architecture baseline.
- [x] Add engineering rules.
- [x] Add technical design baseline.
- [x] Add implementation task tracker.
- [x] Add project memory.
- [x] Add changelog.

## Phase 3 - Hardening
- [x] Audit all tenant/school-scoped repositories.
- [x] Audit controllers for authorization and input validation.
- [x] Add/strengthen service-layer tests for state transitions.
- [x] Add consistent API error handling.
- [x] Review database migration coverage.
- [x] Review AI orchestrator configuration and secret handling.
- [x] Add health/readiness checks where missing.
- [x] Review Docker configuration for production safety.

### Tenant audit result
- School-owned list queries use schoolId predicates.
- Entity lookups by ID are followed by explicit school ownership checks before returning or mutating data.
- Cross-entity lookups are chained through school ownership validation.
- Notification worker lookups use the persisted notification school boundary.
- Controllers consistently enforce JWT tenant access before delegating to services.
- Mutating endpoints use role-based method security.
- Request DTOs use Bean Validation; audited free-form fields have bounded lengths and staff email uses email validation.
- AI question inputs are bounded to prevent oversized requests.
- Controller audit did not identify an unprotected tenant endpoint in the reviewed backend paths.

### Docker production safety
- Application container runs as non-root UID `10001`.
- Application filesystem is read-only except for the document volume and ephemeral `/tmp`.
- Application drops all Linux capabilities and enables `no-new-privileges`.
- Database and Redis ports are bound to localhost in the Compose baseline instead of all host interfaces.
- Docker image healthcheck uses the application liveness endpoint.

### Health/readiness coverage
- Actuator liveness and readiness probes are explicitly configured in `application.yml`.
- Docker healthcheck uses the liveness endpoint.
- CI smoke tests wait for liveness and report readiness status.
- Added a Spring Boot regression test verifying both health groups are exposed by the application context.

### AI orchestrator secret/configuration hardening
- AI orchestrator now fails closed when `OPENAI_API_KEY` is missing.
- MCP URL validation requires HTTP(S); non-local HTTP requires explicit opt-in.
- Added `.env.example` and repository ignore rules so local secrets are not committed.
- Added Python configuration regression tests.

### Database migration coverage
- Flyway integration test now requires all 16 versioned migrations to apply successfully.
- Schema assertions cover all current JPA-backed tables plus the vector store.
- Critical lifecycle/notification/document columns and required PostgreSQL extensions are verified.

### API error-handling coverage
- Standard `ApiError` contract with timestamp, status, error, message, path, requestId and validation errors.
- Consistent handling for application `400/403/404/409/500` responses.
- Bean Validation and constraint-violation details are returned as bounded field/message entries.
- Internal exception details are not exposed by generic or persistence error handlers.
- `X-Request-ID` is preserved when safe and generated when absent/invalid.

### Service state-transition test coverage
- Assessment: DRAFT -> PUBLISHED, invalid status rejection, CANCELLED -> PUBLISHED rejection, tenant-scoped lookup.
- Academic year: activation, single-active-year rule, tenant-scoped lookup.
- Staff: activation/deactivation and tenant-scoped status mutation.
- Notifications: recipient-only read mutation and tenant-scoped lookup.
- Existing student and fee tests continue to cover important status/payment transitions.

## Phase 4 - AI
- [x] Define AI orchestration API contract.
- [x] Add secure backend-to-orchestrator authentication.

### AI orchestrator authentication
- Backend sends a dedicated `Authorization: Bearer` service credential from environment configuration.
- Python validates the credential with constant-time comparison and fails closed when unconfigured.
- Invalid credentials return 401; missing server configuration returns 503.
- The service credential is never copied into tenant/user request bodies or logged.
- [x] Add retrieval/RAG boundary.
- [x] Add AI audit/observability.

- [x] Add domain-safe school context filtering.


### AI audit/observability
- AI ask and RAG indexing operations emit tenant/user-scoped audit events using the existing audit_events table.
- Audit payloads contain correlation, outcome, latency and bounded size/count metadata only; prompts, answers, credentials and document content are never persisted.
- AI request/failure and audit-write-failure Micrometer counters are exposed through the existing actuator metrics surface.
- Audit persistence is best-effort and never replaces or masks the primary AI operation failure.

### Retrieval/RAG boundary
- Retrieval is strictly school-scoped and limited to published documents.
- Document indexing accepts only supported audience roles.
- Retrieval rejects unknown JWT roles instead of broadening access.
- Retrieved document metadata is revalidated against the school, publication status and audience before evidence is returned.
- Query length and top-K are bounded at the retrieval boundary.

### AI orchestration contract
- Contract CI remediation aligned Java HTTP testing and LangChain/MCP dependency compatibility.
- Backend client contract carries requestId, schoolId, userId, roles, message and optional conversationId.
- Trusted context is propagated as X-Request-ID, X-School-ID, X-User-ID and X-User-Roles.
- Python validates that trusted headers match the JSON tenant/user/role context.
- LangGraph state carries request, tenant, user and role context into orchestration instructions.
- Backend connect/read timeouts are configurable and transport failures map to a stable client exception.
- Authentication is the next batch; the internal endpoint must remain on a private network until authenticated.

### Domain-safe school context
- AI prompts are explicitly bounded to the school-management domain.
- Retrieved evidence is filtered before prompt construction by valid document ID, item count and total context size.
- Evidence metadata and content are sanitized and bounded before reaching the model.
- Retrieved content remains untrusted and cannot issue tool, role-change or prompt instructions.

## Working Rule
When the user says "next implementation" or "next batch", select the highest-priority unchecked task, inspect the current code first, implement it, run/inspect CI, fix failures, and update this tracker.
