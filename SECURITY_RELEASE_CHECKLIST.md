# AMSS Commercial Release Security Checklist

Use this checklist before giving any AMSS release to a customer.

## Secrets
- [ ] No real API keys in source files.
- [ ] No database passwords in source files.
- [ ] No JWT secrets in source files.
- [ ] No cloud access keys in source files.
- [ ] No SMTP or SMS credentials in source files.
- [ ] No populated .env files committed.

## Git history
- [ ] Search the complete Git history for secrets.
- [ ] If a secret was ever committed, rotate/revoke it before delivery.
- [ ] Deleting a file does not remove a secret from Git history.

## Configuration
- [ ] Production examples contain placeholders only.
- [ ] Customer-specific URLs and credentials are removed.
- [ ] Public-facing deployment uses HTTPS.

## Database
- [ ] Database credentials are runtime secrets.
- [ ] PostgreSQL is not publicly exposed.
- [ ] Redis is not publicly exposed.
- [ ] Backups are configured.
- [ ] Flyway migrations are included.

## Authentication and authorization
- [ ] JWT secret is customer-specific.
- [ ] RBAC is enabled.
- [ ] Tenant isolation is verified.
- [ ] Admin endpoints are protected.
- [ ] Audit functionality is enabled where required.

## AI
- [ ] AI provider keys are runtime secrets.
- [ ] Customer understands third-party model costs.
- [ ] Sensitive-data handling is reviewed.
- [ ] RAG/vector data access is tenant-aware.
- [ ] MCP tools expose only intended operations.

## Documents
- [ ] Storage permissions are restricted.
- [ ] Customer storage bucket/path is isolated.
- [ ] Upload size limits are configured.
- [ ] Database metadata and file backups are coordinated.

## Delivery
- [ ] CI is green.
- [ ] Tests pass.
- [ ] Build passes.
- [ ] Docker image builds.
- [ ] Installation guide is included.
- [ ] Runbook is included.
- [ ] Commercial license is included.
- [ ] Release/tag is created.
- [ ] Customer repository is private.
