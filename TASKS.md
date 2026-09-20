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
- [ ] Review AI orchestrator configuration and secret handling.
- [ ] Add health/readiness checks where missing.
- [ ] Review Docker configuration for production safety.

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
- [ ] Define AI orchestration API contract.
- [ ] Add secure backend-to-orchestrator authentication.
- [ ] Add retrieval/RAG boundary.
- [ ] Add AI audit/observability.
- [ ] Add domain-safe school context filtering.

## Working Rule
When the user says "next implementation" or "next batch", select the highest-priority unchecked task, inspect the current code first, implement it, run/inspect CI, fix failures, and update this tracker.
