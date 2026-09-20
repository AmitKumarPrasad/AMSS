# AMSS Operational Runbooks

This runbook covers the minimum repeatable procedures for deployment, rollback, database/document backup and restore, and incident response for the production Compose baseline.

## 1. Pre-deployment checklist

Before every production release:

1. Confirm the target image tag and source commit.
2. Confirm CI for that commit is green.
3. Confirm production secrets are present in the runtime secret manager:
   - `POSTGRES_PASSWORD`
   - `OPENAI_API_KEY`
   - `JWT_SECRET` (minimum 32 random characters)
   - `JWT_ISSUER`
   - `LANGGRAPH_BASE_URL`
4. Confirm the image is available to the production host.
5. Confirm recent database and document-storage backups exist.
6. Confirm the change has an owner and a rollback decision point.
7. Confirm the database migration is backward-compatible with the previous application version when a rolling rollback is required.

## 2. Deployment

### Standard deployment

On the production host:

```bash
docker compose --env-file .env.production -f docker-compose.production.yml pull
docker compose --env-file .env.production -f docker-compose.production.yml up -d
docker compose --env-file .env.production -f docker-compose.production.yml ps
```

Verify application health:

```bash
curl -fsS http://127.0.0.1:8080/actuator/health/liveness
curl -fsS http://127.0.0.1:8080/actuator/health/readiness
```

If the application is behind a TLS reverse proxy/load balancer, validate the public HTTPS endpoint and authentication flow before declaring the release successful.

### Post-deployment checks

- Application container is healthy.
- PostgreSQL and Redis containers are healthy.
- Readiness reports success.
- Authentication succeeds with a test account.
- A tenant-scoped read succeeds.
- A tenant-scoped cross-school access attempt is denied.
- A non-destructive financial read succeeds.
- AI requests either succeed or fail with the expected controlled error when the orchestrator is unavailable.
- No unexpected error spike is visible in application logs/metrics.

## 3. Rollback

### Application-only rollback

Use this when the database schema remains compatible with the previous release.

1. Identify the last known-good image tag.
2. Stop the current application container only if required.
3. Update the production deployment to the known-good image.
4. Recreate the application service:

```bash
docker compose --env-file .env.production -f docker-compose.production.yml up -d app
```

5. Verify liveness/readiness and the post-deployment checks above.
6. Record the failed release tag, commit, symptoms, and rollback time.

Do not roll back the application across an incompatible Flyway migration. If the release contains a destructive or irreversible schema change, restore using the database recovery procedure and coordinate the rollback with the release owner.

### Emergency stop

If the application is causing active data corruption or security exposure, remove it from external traffic at the reverse proxy/load balancer first. Preserve logs and evidence before destructive cleanup.

## 4. PostgreSQL backup

Use a PostgreSQL client version compatible with the production server. Store backups outside the application container and encrypt them using the organization's approved storage/KMS process.

Logical backup example:

```bash
docker compose --env-file .env.production -f docker-compose.production.yml exec -T postgres \
  pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc > "backup-$(date +%Y%m%d-%H%M%S).dump"
```

After each backup:

1. Check the command exit status.
2. Record backup timestamp and database version.
3. Store the dump outside the host where possible.
4. Apply the retention policy.
5. Periodically perform a test restore in an isolated environment.

A backup is not considered verified until a restore test succeeds.

## 5. PostgreSQL restore

Restore only after confirming the incident/recovery decision and preserving the current state where possible.

1. Stop application traffic.
2. Preserve the current database state if forensic or reconciliation work may be required.
3. Restore the selected backup into an isolated or replacement database where practical.
4. Validate Flyway schema state and critical tables.
5. Validate tenant counts and representative financial records.
6. Point the application to the restored database.
7. Start the application and verify liveness/readiness.
8. Run authentication, tenant-isolation and financial read checks.
9. Re-enable external traffic only after validation.
10. Record the restore point, operator, duration and data-loss window.

For production recovery, prefer a managed PostgreSQL point-in-time recovery capability when available rather than relying only on logical dumps.

## 6. Document-storage backup and restore

Document metadata is stored in PostgreSQL, while file content may be stored using the configured storage provider.

### Local filesystem

Back up the configured document root together with the database backup:

```bash
tar -czf "documents-$(date +%Y%m%d-%H%M%S).tar.gz" ./data/documents
```

For production, copy the archive to durable external storage. Do not treat the Docker volume alone as a backup.

### S3-compatible storage

Enable bucket versioning/object-lock or the organization's equivalent retention controls where required. Back up or replicate the bucket according to the storage provider's durability and disaster-recovery policy.

Restore both database metadata and document objects from compatible recovery points; restoring only one side can create missing or orphaned documents.

## 7. Incident response

### Severity 1 — active security or data-integrity impact

Examples: cross-tenant data exposure, credential compromise, destructive writes, suspected data corruption.

1. Remove the affected application from external traffic.
2. Do not delete logs, containers, database snapshots or storage objects.
3. Rotate exposed credentials/tokens through the secret manager.
4. Preserve request IDs, timestamps and relevant audit events.
5. Identify affected tenants and data.
6. Stop unsafe background processing if required.
7. Decide whether database restore or point-in-time recovery is necessary.
8. Validate tenant isolation and data integrity before recovery.
9. Restore traffic gradually.
10. Record the incident timeline and follow-up remediation.

### Severity 2 — major service degradation

Examples: repeated 5xx responses, database saturation, AI orchestrator outage, notification backlog.

1. Confirm scope using health endpoints, logs and metrics.
2. Check PostgreSQL, Redis and AI-orchestrator health.
3. Inspect recent deployment changes.
4. Roll back the application if the failure correlates with the latest release and the schema is compatible.
5. Restore normal traffic only after readiness and representative business checks pass.

### Severity 3 — localized defect

Examples: one non-critical workflow failing while core operations remain available.

1. Capture request ID, tenant, endpoint and timestamp.
2. Reproduce without modifying production data where possible.
3. Create a tracked fix.
4. Deploy through normal CI and release controls.
5. Verify the affected workflow and regression checks.

## 8. Incident evidence and audit

For every production incident, record:

- incident ID
- start/end timestamps
- affected release/image and commit
- affected services/tenants
- request IDs and relevant audit-event IDs
- symptoms and detection source
- mitigation and rollback actions
- backup/restore points used
- estimated data-loss window, if any
- root cause or current hypothesis
- corrective and preventive actions
- owner and due date

Never place passwords, API keys, JWT secrets or raw authentication tokens in incident tickets or logs.

## 9. Recovery validation checklist

A recovery is complete only when:

- liveness is healthy
- readiness is healthy
- database connectivity is healthy
- authentication works
- tenant isolation checks pass
- representative student/academic reads work
- representative fee/payment reads work
- document metadata and representative file retrieval work
- notification processing is healthy
- AI failure behavior is controlled if the orchestrator remains unavailable
- monitoring/logging is receiving new events
- the incident record contains the recovery evidence

## 10. Operational ownership

The production owner should review this runbook whenever deployment architecture, database topology, document storage, authentication, or recovery tooling changes. The runbook is a baseline and must be adapted to the organization's managed database, secret manager, TLS/load-balancer, monitoring and disaster-recovery services.
