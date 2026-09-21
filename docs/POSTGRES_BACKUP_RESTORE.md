# PostgreSQL Backup and Restore

The AMSS production baseline uses PostgreSQL as the system of record. Backups should be stored outside the database host and protected by production secret-management and storage controls.

## Backup

The repository provides a custom-format backup script:

    export PGHOST=localhost
    export PGPORT=5432
    export PGUSER=edusphere
    export PGDATABASE=edusphere
    export PGPASSWORD="<database-password>"
    bash scripts/postgres-backup.sh ./backups/edusphere-$(date -u +%Y%m%dT%H%M%SZ).dump

## Restore

Use a tested backup file:

    bash scripts/postgres-restore.sh ./backups/edusphere-20260921T120000Z.dump

The restore script uses --clean --if-exists and does not change ownership or ACLs. Run it against an isolated restore target first when performing disaster recovery validation.

## Production requirements

- Store backup artifacts in durable external storage, not the application container or PostgreSQL PVC.
- Encrypt backups at rest and in transit.
- Restrict access to the database password and backup storage credentials.
- Define retention and deletion policies appropriate to the deployment.
- Test restore procedures regularly; a successful backup alone does not establish recoverability.
- For managed PostgreSQL, prefer provider automated backups and point-in-time recovery where available, while retaining this script for logical export/restore workflows.

## CI validation

CI starts an ephemeral PostgreSQL 17 container, creates representative data, runs the backup script, recreates the database, runs the restore script, and verifies the restored row. This validates that the repository backup/restore contract remains executable.
