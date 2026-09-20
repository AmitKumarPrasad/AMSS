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
- [ ] Audit all tenant/school-scoped repositories.
- [ ] Audit controllers for authorization and input validation.
- [ ] Add/strengthen service-layer tests for state transitions.
- [ ] Add consistent API error handling.
- [ ] Review database migration coverage.
- [ ] Review AI orchestrator configuration and secret handling.
- [ ] Add health/readiness checks where missing.
- [ ] Review Docker configuration for production safety.

## Phase 4 - AI
- [ ] Define AI orchestration API contract.
- [ ] Add secure backend-to-orchestrator authentication.
- [ ] Add retrieval/RAG boundary.
- [ ] Add AI audit/observability.
- [ ] Add domain-safe school context filtering.

## Working Rule
When the user says "next implementation" or "next batch", select the highest-priority unchecked task, inspect the current code first, implement it, run/inspect CI, fix failures, and update this tracker.
