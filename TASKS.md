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
  - Added `.env.production.example` with secret placeholders and no committed credentials.
  - Added `docker-compose.production.yml` with private database/Redis networking, required secrets, read-only app filesystem, dropped capabilities and healthchecks.
  - Documented production startup, liveness/readiness checks and external infrastructure requirements in README.md.
- [x] Operational runbooks for deployment, rollback, backup/restore and incident response.
  - Added `RUNBOOK.md` covering pre-deployment checks, deployment, rollback, PostgreSQL backup/restore, document-storage recovery, incident response and recovery validation.
  - Included evidence-preservation, secret-handling and tenant-isolation checks.
- [ ] End-to-end integration coverage for critical authentication, tenant isolation and financial workflows.
- [ ] SMS provider integration.
- [ ] Richer analytics.

## Working Rule
When the user says "next implementation" or "next batch", select the highest-priority unchecked task, inspect the current code first, implement it, run/inspect CI, fix failures, and update this tracker.
