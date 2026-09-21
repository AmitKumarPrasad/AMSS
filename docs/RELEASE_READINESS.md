# AMSS Backend Release Readiness

## Status

The AMSS backend implementation and CI hardening roadmap is complete as of PR #56.

This document defines the boundary between repository-level engineering readiness and environment-specific production launch work.

## Repository readiness

- Spring Boot backend builds and the full Maven test suite is a required CI gate.
- Python AI orchestrator compiles in CI.
- JWT security configuration is fail-closed and issuer validation is covered.
- Tenant/school authorization and critical financial/authentication flows have regression coverage.
- OpenAPI contract validation is enforced in CI.
- Production smoke tests validate liveness, readiness, authentication and OpenAPI contracts.
- Production container validation requires non-root UID 10001 and a liveness healthcheck.
- Kubernetes manifests cover the application, PostgreSQL and Redis.
- Application PodDisruptionBudget and startup/liveness/readiness probes are validated in CI.
- Prometheus metrics exposure and Kubernetes scrape metadata are validated.
- PostgreSQL backup/drop/restore verification runs against PostgreSQL 17 in CI.

## Environment-specific launch checklist

These items require real deployment credentials/infrastructure and are intentionally outside the repository implementation gate:

1. Provision the production Kubernetes cluster or managed container platform.
2. Provision managed PostgreSQL and Redis where required.
3. Configure production secrets through the approved secret-management system.
4. Configure object storage and production file-storage credentials.
5. Configure real notification provider credentials and sender identities.
6. Configure production OpenAI/AI provider credentials and usage limits.
7. Configure DNS, TLS certificates and ingress/load-balancing.
8. Configure production monitoring, alerting and log retention.
9. Execute a production-like backup restore drill and record the result.
10. Complete UAT and load/performance testing before public launch.

## Frontend transition

With the backend roadmap complete, frontend implementation can begin against the stabilized API contracts.

Recommended frontend sequence:

1. Authentication and session handling.
2. Role-aware application shell and dashboard.
3. Students and guardians.
4. Staff and leave management.
5. Classes, sections and subjects.
6. Attendance.
7. Assessments and report cards.
8. Fees and payments.
9. Notifications.
10. Documents and communication.
11. AI Assistant and conversation history.
12. Settings, responsive behavior, accessibility and end-to-end frontend testing.

The frontend should consume the existing REST/OpenAPI contracts rather than duplicating backend business rules.
