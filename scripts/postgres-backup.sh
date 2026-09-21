#!/bin/sh
set -euo pipefail

: "${PGHOST:?PGHOST is required}"
: "${PGPORT:=5432}"
: "${PGUSER:?PGUSER is required}"
: "${PGDATABASE:?PGDATABASE is required}"
: "${PGPASSWORD:?PGPASSWORD is required}"
BACKUP_FILE="${1:-./backups/${PGDATABASE}-$(date -u +%Y%m%dT%H%M%SZ).dump}"

mkdir -p "$(dirname "$BACKUP_FILE")"
umask 077
pg_dump --format=custom --no-owner --no-acl --file="$BACKUP_FILE"

echo "PostgreSQL backup created: $BACKUP_FILE"
