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

### Security and hardening
- Audited school-scoped repository access across the backend.
- Verified school ownership checks on cross-entity ID lookups.
- Verified school-scoped list queries for tenant-owned data.
- Completed controller authorization and input-validation audit.
- Added bounded request-field validation to student, staff, communication and attendance APIs.
- Added staff email validation and bounded contact fields.
- Recorded the hardening result in TASKS.md.
