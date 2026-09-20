# AMSS Engineering Rules

## Mandatory
1. Never commit API keys, passwords, tokens, or private credentials.
2. Preserve tenant/school isolation in every tenant-owned read and write.
3. Validate externally supplied IDs, strings, dates, enums, and state transitions.
4. Use repository methods that include the tenant/school boundary where applicable.
5. Do not bypass service-layer business rules from controllers.
6. Keep AI-agent code in the Python AI orchestrator; do not move agent execution into Spring Boot unless the architecture is deliberately revised.
7. Do not introduce a dependency merely to solve a local problem when the existing stack already supports it.
8. Avoid generated or escaped source text that cannot compile as normal source files.
9. Every code change must leave the project buildable.
10. CI must be checked after each implementation batch.

## API Rules
- Use appropriate HTTP status codes.
- Return stable DTOs/views rather than exposing persistence entities unnecessarily.
- Reject invalid state transitions explicitly.
- Use consistent error responses.

## Database Rules
- Tenant ownership must be explicit.
- Schema changes must be versioned through the project's migration strategy.
- Avoid destructive migrations without an explicit migration plan.

## Git Rules
- Use focused commit messages.
- Do not rewrite unrelated files.
- Fix the root cause of CI failures instead of masking failures.
