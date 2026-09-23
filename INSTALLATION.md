# AMSS Installation & Customer Setup Guide

## 1. Prerequisites
Recommended baseline:
- Java version required by the current Maven build
- Maven
- Docker Engine
- Docker Compose
- Git
- PostgreSQL 17 / pgvector through the supplied Compose configuration
- Redis 8
- LLM provider API key when AI functionality is enabled
- LangGraph service when AI orchestration is enabled

## 2. Configure secrets
Copy .env.production.example into the secret-management workflow. Required production values include POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD, JWT_SECRET, JWT_ISSUER, OPENAI_API_KEY when AI is enabled, and LANGGRAPH_BASE_URL when orchestration is enabled.

Never commit populated production secrets.

## 3. Local development
Use the development Compose configuration:
~~~bash
docker compose up -d
docker compose ps
curl -fsS http://127.0.0.1:8080/actuator/health/liveness
curl -fsS http://127.0.0.1:8080/actuator/health/readiness
~~~

## 4. Production deployment
Build/tag the application image:
~~~bash
docker build -t amss:production .
~~~
Start the production stack:
~~~bash
docker compose --env-file .env.production -f docker-compose.production.yml up -d
~~~

A public deployment should normally sit behind TLS termination and an appropriate reverse proxy/load balancer.

## 5. Database
Flyway migrations are used by the application. Do not modify the production schema manually unless the deployment/recovery procedure explicitly requires it.

## 6. Document storage
Development may use local filesystem storage. For multi-instance production deployments, use the configured S3-compatible storage integration where appropriate. Keep database metadata and document objects backed up together.

## 7. AI configuration
AI features may require OPENAI_API_KEY, LANGGRAPH_BASE_URL, PostgreSQL/pgvector and appropriate model/provider configuration. Model usage incurs separate provider charges.

## 8. Security checklist
- Use a unique strong database password.
- Use a randomly generated JWT secret of at least 32 characters.
- Use HTTPS.
- Keep PostgreSQL and Redis private.
- Store secrets outside Git.
- Configure backups.
- Configure monitoring and alerting.
- Verify tenant isolation and RBAC.
- Review document storage permissions.
- Review AI data handling and provider policies.

## 9. Upgrade procedure
1. Back up PostgreSQL.
2. Back up document storage.
3. Record the current application image/tag.
4. Deploy the new release.
5. Verify migrations.
6. Verify liveness/readiness.
7. Test authentication and tenant isolation.
8. Test representative school workflows.
9. Test AI behavior if enabled.
10. Monitor logs and metrics.
11. Keep the previous release available for rollback where schema compatibility permits.

For detailed rollback and recovery procedures, see RUNBOOK.md.
