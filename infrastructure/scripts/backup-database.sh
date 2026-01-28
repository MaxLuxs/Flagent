#!/bin/bash
# Flagent Database Backup Script
# Automated daily backup with S3 upload

set -e

# Configuration
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-flagent}
DB_USER=${DB_USER:-flagent}
DB_PASSWORD=${DB_PASSWORD:-flagent}
BACKUP_DIR=${BACKUP_DIR:-/var/backups/flagent/db}
RETENTION_DAYS=${RETENTION_DAYS:-30}
S3_BUCKET=${S3_BUCKET:-}

# Create backup directory
mkdir -p "$BACKUP_DIR"

# Timestamp
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/flagent_${TIMESTAMP}.sql.gz"

echo "[$(date -Iseconds)] Starting database backup..."

# Create backup
PGPASSWORD="$DB_PASSWORD" pg_dump \
  -h "$DB_HOST" \
  -p "$DB_PORT" \
  -U "$DB_USER" \
  -d "$DB_NAME" \
  --format=custom \
  --compress=9 \
  | gzip > "$BACKUP_FILE"

# Verify backup
if [ -f "$BACKUP_FILE" ]; then
  SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
  echo "[$(date -Iseconds)] Backup created successfully: $SIZE"
  
  # Log to audit file
  echo "$(date -Iseconds),BACKUP,SUCCESS,${BACKUP_FILE},${SIZE}" >> "$BACKUP_DIR/../backup-audit.log"
else
  echo "[$(date -Iseconds)] ERROR: Backup failed!"
  echo "$(date -Iseconds),BACKUP,FAILED,${BACKUP_FILE}," >> "$BACKUP_DIR/../backup-audit.log"
  exit 1
fi

# Cleanup old backups
echo "[$(date -Iseconds)] Cleaning up backups older than $RETENTION_DAYS days..."
find "$BACKUP_DIR" -name "flagent_*.sql.gz" -mtime +"$RETENTION_DAYS" -delete

# Upload to S3 (optional)
if [ -n "$S3_BUCKET" ]; then
  echo "[$(date -Iseconds)] Uploading to S3: s3://$S3_BUCKET/backups/"
  aws s3 cp "$BACKUP_FILE" "s3://$S3_BUCKET/backups/" || {
    echo "[$(date -Iseconds)] WARNING: S3 upload failed"
  }
fi

echo "[$(date -Iseconds)] Backup completed successfully"
