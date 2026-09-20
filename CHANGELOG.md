# Changelog

## 2026-09-20

### Backend AI assistant and conversation state
- Added tenant- and user-scoped AI conversation persistence.
- Added bounded conversation listing and message history.
- Added create, list, get and delete conversation APIs.
- Added conversation-aware assistant requests with persistent user/assistant turns.
- Preserved JWT tenant authorization and user ownership boundaries.
- Reused existing backend-to-orchestrator service authentication and request correlation.

### Backend documents and communication
- Added bounded document listing and request field limits.
- Added paginated tenant-scoped announcements.
- Preserved audience filtering, document publishing/archive transitions and school isolation.

### Backend notifications completion
- Added bounded notification listing and strict notification request field limits.
- Preserved tenant-scoped recipient authorization and recipient-only read mutation.
- Preserved retry/backoff limits and fail-closed email/SMS provider controls.

### Backend assessments and report cards
- Added bulk assessment-result recording with duplicate-student protection.
- Added marks and grade validation with assessment-state enforcement.
- Added tenant-scoped academic-year report-card aggregation.

### Backend fees and payments completion
- Added tenant-scoped school invoice pagination with bounded page sizes and status filtering.
- Added bounded payment-history retrieval.
- Preserved active-student invoice creation, duplicate payment-reference protection and overpayment rejection.

### Backend attendance completion
- Added tenant-authorized bulk student attendance marking with duplicate-entry protection.
- Added bounded 366-day attendance history validation.
- Added tenant-scoped daily attendance summary API.
- Preserved active-student and future-date validation for attendance writes.

## 2026-09-20

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

### Production security configuration
- Added fail-closed JWT secret validation and configured JWT issuer validation.

### AI hardening
- Added secure backend-to-orchestrator authentication, RAG boundaries, AI audit observability and domain-safe school context filtering.

### API and database hardening
- Added centralized API error handling and safe request correlation IDs.
- Strengthened Flyway migration coverage and tenant-scoped validation.
