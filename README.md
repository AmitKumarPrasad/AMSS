# EduSphere AI — School Management Platform

Production-oriented school management platform built as a polyglot monorepo:

- **Spring Boot**: core school domain, REST APIs, security, persistence, audit and workflows.
- **Spring AI**: model integration, RAG and MCP capabilities.
- **LangGraph service**: stateful AI orchestration for multi-step school workflows.
- **MCP**: controlled AI access to school tools and resources.
- **PostgreSQL + pgvector**: transactional data and semantic retrieval.
- **Redis**: cache, conversation/session state and rate limiting.
- **Docker Compose**: reproducible local infrastructure.

## Implemented modules

- Identity & Access: multi-tenant schools, users, roles, JWT authentication and tenant isolation.
- Student Information: student lifecycle, enrollment and guardian/contact management.
- Academics: academic years, classes, sections, subjects, class-subject mapping and timetable.
- Attendance: student and staff attendance recording, upsert and history.
- Assessments: assessments, marks/results, report cards and validation.
- Fees: invoices, payments, balances and duplicate/overpayment protection.
- Communication: announcements, audience targeting and read tracking.
- Documents: registration, publishing, archiving, audience visibility and real file upload/download storage.
- Notifications: in-app notification outbox, read state, scheduling, email delivery and atomic multi-instance claiming.
- Operations: audit events, school dashboard KPIs and operational observability endpoints.
- Admin Web UI: authenticated responsive dashboard for school operations.
- AI Assistant: Spring AI, RAG/vector search, MCP tools and LangGraph orchestration.

## Architecture

~~~text
Web / Mobile / Admin UI
          |
      API Gateway
          |
   Spring Boot Core API
   |       |        |
Postgres Redis   MCP Server
   |                |
 pgvector       School Tools
          \        /
           LangGraph AI
                |
             LLM / RAG
~~~

## Development principles

- Hexagonal/modular architecture rather than controller-driven CRUD.
- DTOs at API boundaries; entities never exposed directly.
- Flyway migrations; no schema mutation from Hibernate in production.
- RBAC + tenant isolation + audit trail.
- Idempotency for financial and notification operations.
- Structured AI outputs and tool schemas.
- Retrieval with metadata filters and source citations.
- Tests for domain, API, security and AI workflows.
- Observability with Actuator/Micrometer.
- Document content is stored behind a storage abstraction; local filesystem is the default provider and S3-compatible object storage is supported without changing the document API contract.

## Production deployment

Use `docker-compose.production.yml` as the deployment baseline for a single application stack. It intentionally does not publish PostgreSQL or Redis ports to the host.

1. Copy `.env.production.example` into your secret-management workflow; do not commit populated secrets.
2. Generate a strong database password and a random JWT secret of at least 32 characters.
3. Set `JWT_ISSUER` to the public HTTPS issuer used by the API.
4. Set `LANGGRAPH_BASE_URL` to the private AI-orchestrator endpoint.
5. Build/tag the application image as `amss:production`.
6. Start with `docker compose --env-file .env.production -f docker-compose.production.yml up -d`.
7. Verify liveness and readiness before routing external traffic.
8. Keep database backups and document storage backups outside the application container.

See **RUNBOOK.md** for deployment, rollback, backup/restore and incident-response procedures.

## Commercial distribution

AMSS is now packaged for commercial source-code licensing.

- **Standard:** $699
- **Professional:** $999
- **Business:** $1,499+
- **Exclusive:** $3,000–$5,000+ negotiated

See:
- **COMMERCIAL_LICENSE.md** — commercial source-code license template
- **SELLING_GUIDE.md** — pricing, buyer qualification and delivery process
- **INSTALLATION.md** — customer setup and deployment guide
- **MARKETPLACE_LISTING.md** — ready-to-use product listing copy
- **SECURITY_RELEASE_CHECKLIST.md** — pre-delivery security checklist
- **RUNBOOK.md** — operational deployment, backup, restore and incident response

The main development repository should remain private. Deliver a tagged release or a separate customer repository according to the purchased license.

## Document storage

Local filesystem remains the default for development:

~~~text
DOCUMENT_STORAGE_PROVIDER=local
DOCUMENT_STORAGE_ROOT=./data/documents
~~~

For multi-instance deployments, use S3 or an S3-compatible service such as MinIO.

The AWS SDK default credential provider chain is used, so credentials should be supplied through the runtime environment, workload identity, or the platform's secret manager. Never commit access keys or secrets.

## Current status

Core school-management bounded contexts, AI foundation, authenticated admin UI, local document storage, S3-compatible object storage, email notifications and atomic notification claiming are implemented and CI-verified. Remaining work is primarily production hardening: SMS provider integration, richer analytics, end-to-end integration coverage and deployment infrastructure beyond the provided Compose baseline.

## Configuration

Set `OPENAI_API_KEY` in the runtime environment. Never commit secrets. Local development should use an ignored `.env`/`.env.local` or IDE environment configuration.

Document uploads are limited by `MAX_FILE_SIZE` (default `25MB`) and `MAX_REQUEST_SIZE` (default `30MB`).


## Documentation and marketplace distribution

Developer and operational documentation is indexed in [docs/MARKETPLACE_QUALITY.md](docs/MARKETPLACE_QUALITY.md), including the clean source-package boundary, coverage evidence, third-party attribution guidance, and links to architecture, API, release-readiness, backup/restore and runbook documentation.

Marketplace source archives should be created from tracked files with `git archive`; local SQLite/database files, generated build output, populated environment files, credentials and caches must not be included.
