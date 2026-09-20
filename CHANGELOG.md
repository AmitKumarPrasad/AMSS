# Changelog

## 2026-09-20

### Admin Web UI
- Modernized the authenticated dashboard with responsive sidebar navigation and mobile layout.
- Added dashboard KPI cards for students, staff, attendance, leaves, fees and unread notifications.
- Added searchable tenant-scoped student directory.
- Added analytics view with bounded date-range controls and attendance/fee trend visualizations.
- Kept the UI on existing authenticated APIs and session-based access token handling.

### Richer analytics
- Added a tenant-scoped analytics API with bounded date ranges.
- Added attendance and staff-attendance trends, fee collection summaries/trends and assessment activity KPIs.
- Added school-level authorization before analytics evaluation and avoided returning student-level identifying data.

### SMS provider integration
- Added Twilio SMS delivery for queued SMS notifications with tenant-scoped recipient lookup.
- Added optional user phone storage through a Flyway migration.
- Added fail-closed SMS configuration and disabled-by-default delivery.

### Critical workflow integration coverage
- Added Testcontainers PostgreSQL integration coverage for school-scoped students, invoices and payments.
- Added duplicate payment-reference protection coverage.
- Added JWT tenant-context coverage for same-school access and cross-school rejection through TenantAccess.
- Added MockMvc coverage for unauthenticated, cross-tenant and unauthorized-role access to fee APIs.

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
