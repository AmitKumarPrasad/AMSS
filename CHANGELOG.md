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

### Database hardening
- Strengthened Flyway integration coverage from a minimum migration count to all 16 versioned migrations.
- Added schema assertions for all current application tables, vector store objects and critical evolved columns.

### API hardening
- Added centralized REST exception handling with a stable `ApiError` response contract.
- Added validation, access-denied, not-found, conflict and generic-error mappings.
- Added safe request-correlation IDs through `X-Request-ID`.
- Added regression tests ensuring internal exception details are not exposed.

### Security and hardening
- Audited school-scoped repository access across the backend.
- Verified school ownership checks on cross-entity ID lookups.
- Verified school-scoped list queries for tenant-owned data.
- Completed controller authorization and input-validation audit.
- Added bounded request-field validation to student, staff, communication and attendance APIs.
- Added staff email validation and bounded contact fields.
- Added tenant and recipient ownership assertions to service-layer state-transition tests.
- Recorded the hardening result in TASKS.md.
