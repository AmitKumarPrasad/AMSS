# Changelog

## 2026-09-20

### Critical workflow integration coverage
- Added Testcontainers PostgreSQL integration coverage for school-scoped students, invoices and payments.
- Added duplicate payment-reference protection coverage.
- Added JWT tenant-context coverage for same-school access and cross-school rejection through TenantAccess.

### Operational runbooks
- Added RUNBOOK.md for deployment, rollback, backup/restore and incident response.

### Production deployment
- Added production environment and Docker Compose deployment baselines.
- Documented production startup and operational prerequisites in README.md.

### Production security configuration
- Added fail-closed JWT secret validation and configured JWT issuer validation.

### AI hardening
- Added secure backend-to-orchestrator authentication, RAG boundaries, AI audit observability and domain-safe school context filtering.

### API and database hardening
- Added centralized API error handling and safe request correlation IDs.
- Strengthened Flyway migration coverage and tenant-scoped validation.