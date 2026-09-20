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
- [ ] Backend-wide pagination, filtering and API contract review.
  - [~] Corrected audience-aware pagination for documents and announcements so inaccessible rows no longer consume a page.
  - [x] Added bounded pagination, status filtering and search to student and staff list APIs with deterministic ordering.
  - [x] Added bounded pagination/status filtering to academic-year lists, pagination to class lists, and pagination/search to subject lists with deterministic ordering.
  - [x] Added bounded pagination, status filtering and search to section list APIs with deterministic ordering.
  - [x] Added bounded pagination and status filtering to staff leave lists with deterministic ordering.
  - [x] Added bounded pagination to assessment list APIs with deterministic assessment-date ordering.
  - [x] Added bounded pagination to enrollment-by-section and enrollment-by-student APIs with deterministic ordering.
  - [ ] Review remaining unbounded list endpoints and response contracts.
- [ ] Backend production integration/security verification.

## Working Rule
When the user says "next implementation" or "next batch", select the highest-priority unchecked task, inspect the current code first, implement it, run/inspect CI, fix failures, and update this tracker.
