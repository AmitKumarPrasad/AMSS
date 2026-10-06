# Run AMSS Locally

This starts PostgreSQL/pgvector, Redis, the Spring Boot API and the LangGraph/FastAPI AI orchestrator.

## 1. Configure environment

```bash
cp .env.example .env
```

Set real values for `OPENAI_API_KEY`, `JWT_SECRET` and `AI_ORCHESTRATOR_SERVICE_TOKEN`.

For a ready-to-test local school account, optionally set:

```text
APP_DEMO_ENABLED=true
APP_DEMO_USERNAME=demo-admin
APP_DEMO_PASSWORD=<choose-a-local-password-of-at-least-12-characters>
```

Demo mode is disabled by default and refuses to start without an explicit password.

## 2. Start backend stack

```bash
docker compose up -d --build
```

Verify:

```bash
curl http://localhost:8080/actuator/health/readiness
curl http://localhost:8123/health
```

Expected endpoints:

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui/index.html
- AI orchestrator: http://localhost:8123
- PostgreSQL: localhost:5432
- Redis: localhost:6379

## 3. Start frontend

Clone `AMSS-Frontend` beside this repository, then:

```bash
cd ../AMSS-Frontend
cp .env.example .env
npm install
npm run dev
```

Open http://localhost:5173.

The backend permits `http://localhost:5173` by default through `CORS_ALLOWED_ORIGINS`.

## 4. Stop

```bash
docker compose down
```

Use `docker compose down -v` only when you intentionally want to remove local PostgreSQL, Redis and document volumes.
