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
  - Added Testcontainers PostgreSQL workflow coverage for tenant-scoped student/invoice/payment records and duplicate payment-reference protection.
  - Added JWT tenant-context coverage proving same-school access and cross-school rejection through TenantAccess.
  - Added MockMvc authorization coverage for unauthenticated, cross-tenant and unauthorized-role access to fee APIs.
- [x] SMS provider integration.
  - Added Twilio SMS delivery for queued SMS notifications.
  - Added tenant-scoped active-user phone lookup and retry-safe failure handling.
  - SMS delivery remains disabled by default until production credentials are configured.
- [x] Richer analytics.
  - Added tenant-scoped date-range analytics for student attendance, staff attendance, fee collection and assessment activity.
  - Added bounded 366-day reporting windows with school authorization at the API boundary.

## Backend Completion Roadmap
- [x] Complete attendance backend APIs with bulk marking, bounded history and tenant-scoped daily summary.
- [x] Complete fees and payments backend APIs.
  - Added tenant-scoped school invoice pagination with bounded page size and status filtering.
  - Added bounded payment history retrieval.
  - Preserved invoice/payment tenant isolation, duplicate-reference protection and overpayment rejection.
- [ ] Complete assessments/report-card backend APIs.
- [ ] Complete notifications backend workflows and provider controls.
- [ ] Complete documents and communication backend APIs.
- [ ] Complete AI assistant production APIs and conversation state.
- [ ] Backend-wide pagination, filtering and API contract review.
- [ ] Backend production integration/security verification.

## Working Rule
When the user says "next implementation" or "next batch", select the highest-priority unchecked task, inspect the current code first, implement it, run/inspect CI, fix failures, and update this tracker.
