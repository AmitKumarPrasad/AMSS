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
- Attendance: student attendance recording, upsert and history.
- Assessments: assessments, marks/results and validation.
- Fees: invoices, payments, balances and duplicate/overpayment protection.
- Communication: announcements, audience targeting and read tracking.
- Documents: registration, publishing, archiving and audience visibility.
- AI Assistant: Spring AI, RAG/vector search, MCP tools and LangGraph orchestration.
- Operations: audit events and operational observability endpoints.

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

## Current status

The core backend bounded contexts and AI foundation are implemented and CI-verified. Remaining work is primarily production-hardening and product completion: frontend/admin UI, persistent file storage, notification delivery providers, staff/leave workflows, richer report-card analytics, end-to-end integration tests, deployment configuration, security review and operational runbooks.

## Configuration

Set `OPENAI_API_KEY` in the runtime environment. Never commit secrets. Local development should use an ignored `.env`/`.env.local` or IDE environment configuration.
