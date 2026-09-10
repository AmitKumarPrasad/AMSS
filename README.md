# EduSphere AI — School Management Platform

Production-oriented school management platform built as a polyglot monorepo:

- **Spring Boot**: core school domain, REST APIs, security, persistence, audit and workflows.
- **Spring AI**: model integration, RAG and MCP client/server capabilities where Java is the best fit.
- **LangGraph service**: stateful AI orchestration for multi-step school workflows.
- **MCP**: controlled AI access to school tools and resources.
- **PostgreSQL + pgvector**: transactional data and semantic retrieval.
- **Redis**: cache, conversation/session state and rate limiting.
- **Docker Compose**: reproducible local infrastructure.

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

## Planned bounded contexts

1. Identity & Access — tenant, school, users, roles, permissions.
2. Student Information — students, guardians, admissions, enrollment.
3. Academics — academic years, classes, sections, subjects, timetable.
4. Attendance — student/staff attendance, leave and alerts.
5. Assessments — exams, grading, report cards and analytics.
6. Fees — fee plans, invoices, payments, receipts and outstanding balances.
7. Communication — notices, announcements, parent/teacher messaging.
8. Documents — policies, circulars, handbooks and knowledge base.
9. AI Assistant — RAG, tool calling, approvals, audit and conversation memory.
10. Operations — notifications, jobs, audit events and observability.

## AI capabilities

The assistant is designed for role-aware questions such as:

- “Show students in Class 8A with attendance below 75%.”
- “Draft a parent notice for tomorrow’s PTM.”
- “What is our leave policy for teaching staff?” — answered from indexed school documents.
- “Which students have outstanding fees above ₹10,000?”
- “Prepare a principal briefing for today.”

The AI must never bypass authorization. Tool access is scoped to the authenticated user, school tenant and role. Destructive actions should use an explicit approval workflow.

## Development principles

- Hexagonal/modular architecture rather than controller-driven CRUD.
- DTOs at API boundaries; entities never exposed directly.
- Flyway migrations; no schema mutation from Hibernate in production.
- RBAC + tenant isolation + audit trail.
- Idempotency for financial and notification operations.
- Structured AI outputs and tool schemas.
- Retrieval with metadata filters and source citations.
- Tests for domain, API, security and AI workflows.
- Observability with Actuator/Micrometer and distributed tracing.

## Configuration

Set `OPENAI_API_KEY` in the runtime environment. Never commit secrets. Local development should use an ignored `.env`/`.env.local` or IDE environment configuration.

## Status

Phase 1 establishes the production architecture and foundation. Subsequent phases add each bounded context and the AI workflows incrementally.
