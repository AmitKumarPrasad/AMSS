# Changelog

## 2026-09-20
### Fixed
- Corrected malformed escaped Java source in the assessment module.
- Added the missing assessment status-change service method required by the controller.
- Confirmed GitHub Actions CI is green on commit 7227aaec12f7ba35404bc90dd39509fd82751b8f.

### Added
- Product requirements baseline.
- Architecture baseline.
- Engineering rules.
- Technical design baseline.
- Implementation task tracker.
- Project memory.
- Service-layer state-transition tests for assessment, academic year, staff and notifications.

### Docker hardening
- Hardened the application container with read-only filesystem, dropped capabilities, no-new-privileges and an explicit healthcheck.
- Restricted local database and Redis host bindings to loopback by default.
- Preserved writable document storage through the dedicated volume and `/tmp` through tmpfs.

### Operations hardening
- Added regression coverage for Actuator liveness/readiness groups.
- Confirmed Docker and CI use the liveness probe while readiness remains available for dependency-aware orchestration.

### AI orchestrator hardening
- Added explicit OpenAI API-key validation and MCP URL safety checks.
- Added local `.env` template and ignore rules for runtime secrets.
- Added Python regression tests for missing secrets and unsafe MCP endpoints.

### Database hardening
- Strengthened Flyway integration coverage from a minimum migration count to all 16 versioned migrations.
- Added schema assertions for all current application tables, vector store objects and critical evolved columns.

### API hardening
- Added centralized REST exception handling with a stable `ApiError` response contract.
- Added validation, access-denied, not-found, conflict and generic-error mappings.
- Added safe request-correlation IDs through `X-Request-ID`.
- Added regression tests ensuring internal exception details are not exposed.

### AI orchestration contract
- Added a typed Spring backend client for the Python orchestration service.
- Added configurable connect/read timeouts and stable transport failure handling.
- Added a FastAPI `/internal/v1/ai/ask` contract with tenant, user, role and correlation validation.
- Propagated trusted school/user/role context into LangGraph state and system instructions.
- Added Java and Python contract regression coverage.
- Authentication remains the next required control before exposing the internal endpoint outside a private network.

### Security and hardening
- Audited school-scoped repository access across the backend.
- Verified school ownership checks on cross-entity ID lookups.
- Verified school-scoped list queries for tenant-owned data.
- Completed controller authorization and input-validation audit.
- Added bounded request-field validation to student, staff, communication and attendance APIs.
- Added staff email validation and bounded contact fields.
- Added tenant and recipient ownership assertions to service-layer state-transition tests.
- Recorded the hardening result in TASKS.md.

### Retrieval/RAG boundary
- Hardened RAG retrieval with strict school and published-document revalidation.
- Restricted document audience metadata to supported roles and rejected unknown retrieval roles.
- Added bounded query/top-K controls at the retrieval service boundary.

### AI orchestrator authentication
- Added fail-closed backend-to-orchestrator service authentication.
- Added constant-time Python token validation and regression coverage.
- Kept service credentials in environment configuration rather than source control or request payloads.

### AI audit and observability
- Added tenant/user-scoped audit events for AI requests and RAG indexing using the existing `audit_events` infrastructure.
- Persisted only correlation, outcome, latency and bounded count/length metadata; prompts, answers, document content and credentials are excluded.
- Added Micrometer counters for AI requests, AI failures and audit persistence failures.
- Audit persistence is best-effort so observability failures do not mask the primary AI operation result.
