#!/bin/sh
set -euo pipefail

: "${PGHOST:?PGHOST is required}"
: "${PGPORT:=5432}"
: "${PGUSER:?PGUSER is required}"
: "${PGDATABASE:?PGDATABASE is required}"
: "${PGPASSWORD:?PGPASSWORD is required}"
BACKUP_FILE="${1:?Usage: postgres-restore.sh <backup-file>}"

test -f "$BACKUP_FILE" || { echo "Backup file not found: $BACKUP_FILE" >&2; exit 1; }

pg_restore --clean --if-exists --no-owner --no-acl --dbname="$PGDATABASE" "$BACKUP_FILE"

echo "PostgreSQL backup restored: $BACKUP_FILE"
