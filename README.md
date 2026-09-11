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

```text
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
```

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

## Document storage

Local filesystem remains the default for development:

```text
DOCUMENT_STORAGE_PROVIDER=local
DOCUMENT_STORAGE_ROOT=./data/documents
```

For multi-instance deployments, use S3 or an S3-compatible service such as MinIO:

```text
DOCUMENT_STORAGE_PROVIDER=s3
DOCUMENT_S3_BUCKET=amss-documents
DOCUMENT_S3_REGION=ap-south-1
DOCUMENT_S3_PREFIX=documents/
# Optional for MinIO or another S3-compatible endpoint:
DOCUMENT_S3_ENDPOINT=http://minio:9000
DOCUMENT_S3_PATH_STYLE_ACCESS=true
```

The AWS SDK default credential provider chain is used, so credentials should be supplied through the runtime environment, workload identity, or the platform's secret manager. Never commit access keys or secrets.

## Current status

Core school-management bounded contexts, AI foundation, authenticated admin UI, local document storage, S3-compatible object storage, email notifications and atomic notification claiming are implemented and CI-verified. Remaining work is primarily production hardening: SMS provider integration, richer analytics, end-to-end integration coverage, deployment configuration, security review and operational runbooks.

## Configuration

Set `OPENAI_API_KEY` in the runtime environment. Never commit secrets. Local development should use an ignored `.env`/`.env.local` or IDE environment configuration.

Document uploads are limited by `MAX_FILE_SIZE` (default `25MB`) and `MAX_REQUEST_SIZE` (default `30MB`).
