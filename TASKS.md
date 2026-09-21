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
- [x] Complete JWT security review: validate issuer configuration and enforce the configured issuer claim during token verification.

## Phase 4 - AI
- [x] Define AI orchestration API contract.
- [x] Add secure backend-to-orchestrator authentication.
- [x] Add retrieval/RAG boundary.
- [x] Add AI audit/observability.
- [x] Add domain-safe school context filtering.

## Production Hardening Roadmap
- [x] Security review: fail closed on weak JWT secrets and enforce configured JWT issuer validation.
- [x] Deployment configuration for production environments.
- [x] Operational runbooks for deployment, rollback, backup/restore and incident response.
- [x] End-to-end integration coverage for critical authentication, tenant isolation and financial workflows.
- [x] SMS provider integration.
- [x] Richer analytics.

## Backend Completion Roadmap
- [x] Complete attendance backend APIs with bulk marking, bounded history and tenant-scoped daily summary.
- [x] Complete fees and payments backend APIs.
- [x] Complete assessments/report-card backend APIs.
- [x] Complete notifications backend workflows and provider controls.
- [x] Complete documents and communication backend APIs.
- [x] Complete AI assistant production APIs and conversation state.
  - Added tenant- and user-scoped conversation creation, listing, retrieval and deletion.
  - Added bounded persistent message history with strict message/title limits.
  - Added conversation-aware AI requests with request correlation and existing service authentication.
  - Preserved school isolation and user ownership for every conversation/message operation.
- [x] Backend-wide pagination, filtering and API contract review.
  - [x] Corrected audience-aware pagination for documents and announcements so inaccessible rows no longer consume a page.
  - [x] Added bounded pagination, status filtering and search to student and staff list APIs with deterministic ordering.
  - [x] Added bounded pagination/status filtering to academic-year lists, pagination to class lists, and pagination/search to subject lists with deterministic ordering.
  - [x] Added bounded pagination, status filtering and search to section list APIs with deterministic ordering.
  - [x] Added bounded pagination and status filtering to staff leave lists with deterministic ordering.
  - [x] Added bounded pagination to assessment list APIs with deterministic assessment-date ordering.
  - [x] Added bounded pagination to enrollment-by-section and enrollment-by-student APIs with deterministic ordering.
  - [x] Added bounded pagination to student-specific fee invoice lists with deterministic due-date ordering.
  - [x] Added bounded pagination to timetable section list APIs with deterministic day/time/id ordering.
  - [x] Added bounded pagination to student guardian list APIs with deterministic primary-contact/name/id ordering.
  - [x] Added bounded database-level pagination to invoice payment history with deterministic paidAt/id ordering.
  - [x] Reviewed remaining unbounded list endpoints and removed the unused staff-scoped leave List query.
  - [x] Added regression tests that verify audience filtering occurs at database pagination boundaries for documents and announcements.
- [x] Backend production integration/security verification.
  - [x] Added JWT configuration regression coverage for strong secrets and HTTPS production issuers.
  - [x] Added fail-closed coverage for weak/default secrets, blank issuers and non-HTTPS production issuers.
  - [x] Verified backend compilation/tests, production container build, compose validation and application smoke test in CI.
- [x] Restore the full Maven test suite, including integration tests, as a required CI gate.
  - [x] Restored `mvn -B test package` in GitHub Actions.
  - [x] Added Spring Boot 4 security test support and regression coverage for fee endpoint authentication, role authorization and tenant-boundary denial.
  - [x] Verified full Maven tests, production container build, compose validation and application smoke test in CI.
- [x] Add CI OpenAPI contract regression validation.
  - [x] Validate the generated OpenAPI document during the application smoke test.
  - [x] Verify API metadata and critical authentication/AI endpoints are present.
  - [x] PR #49 merged after CI Run #483 completed successfully.

- [x] Add production smoke-test authentication contract validation.
  - [x] Verify protected API endpoints reject unauthenticated requests.
  - [x] Verify login input validation rejects malformed credentials.
  - [x] PR #50 merged after CI Run #486 completed successfully.

- [x] Make production smoke-test readiness a required CI gate.
  - [x] Require `/actuator/health/readiness` to return HTTP 200.
  - [x] Keep authentication and OpenAPI contract checks in the same smoke gate.
  - [x] PR #51 merged after CI Run #489 completed successfully.

- [x] Add CI production-container hardening contract validation.
  - [x] Verify the built image runs as non-root UID 10001.
  - [x] Verify the image contains a liveness healthcheck.
  - [x] PR #52 merged after CI Run #492 completed successfully.

- [x] Add production deployment readiness baseline.
  - [x] Add Kubernetes manifests for the application, PostgreSQL, and Redis connectivity configuration.
  - [x] Add deployment validation to CI by rendering and syntax-validating the Kubernetes manifests with Kustomize.
  - [x] Document the deployment baseline and required secrets.
  - [x] PR #53 merged after CI Run #499 completed successfully.

- [x] Add production observability baseline.
  - [x] Add Micrometer Prometheus registry and expose Prometheus metrics through the internal application service.
  - [x] Add Kubernetes Prometheus scrape metadata for the application service.
  - [x] Require CI smoke tests to validate the Prometheus endpoint and representative JVM metrics.
  - [x] PR #54 merged after CI Run #503 completed successfully after fixing the missing Prometheus registry dependency.

- [x] Add PostgreSQL backup and restore validation baseline.
  - [x] Add reusable custom-format backup and restore scripts.
  - [x] Document production backup, retention, encryption and restore requirements.
  - [x] Require CI to execute a real backup/drop/restore verification against PostgreSQL 17.
  - [x] PR #55 merged after CI Run #512 completed successfully after fixing the PostgreSQL CI password/environment contract.

## Working Rule
When the user says "next implementation" or "next batch", select the highest-priority unchecked task, inspect the current code first, implement it, run/inspect CI, fix failures, and update this tracker.
