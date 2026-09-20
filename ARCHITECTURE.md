# AMSS Architecture

## System Context

AMSS is organized around a Java/Spring Boot transactional backend and a separate Python AI orchestrator.

```
Client
  |
  v
Spring Boot REST API
  |
  +-- Authentication / Authorization
  +-- School/Tenant boundary
  +-- Academic modules
  +-- Administrative modules
  |
  +-- PostgreSQL/MySQL persistence
  |
  +----> Python AI Orchestrator
             |
             +-- LLM providers
             +-- RAG / vector store
             +-- AI tools and workflows
```

## Backend Principles
- Keep business logic in services.
- Keep HTTP concerns in controllers.
- Keep persistence in repositories.
- Use DTO/view models at API boundaries.
- Scope tenant-owned data by school/tenant identifier.
- Prefer transactional service methods for state-changing operations.
- Centralize security and error handling.

## AI Orchestrator
The AI layer is Python/FastAPI based and independently deployable. It should not require Spring Boot for AI-agent execution.

## Data
Relational persistence is the source of truth for transactional school data. Redis/vector storage may be introduced for caching and retrieval workloads without replacing the transactional source of truth.

## Deployment
The repository supports containerized development through Docker/Docker Compose and CI validation through GitHub Actions.
