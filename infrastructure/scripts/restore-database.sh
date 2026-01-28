#!/bin/bash
# Flagent Database Restore Script

set -e

BACKUP_FILE=$1
DB_NAME=${DB_NAME:-flagent}
DB_USER=${DB_USER:-flagent}
DB_PASSWORD=${DB_PASSWORD:-flagent}
DB_HOST=${DB_HOST:-localhost}

if [ -z "$BACKUP_FILE" ]; then
  echo "Usage: $0 <backup_file.sql.gz>"
  echo ""
  echo "Available backups:"
  ls -lht /var/backups/flagent/db/*.sql.gz | head -5
  exit 1
fi

if [ ! -f "$BACKUP_FILE" ]; then
  echo "ERROR: Backup file not found: $BACKUP_FILE"
  exit 1
fi

echo "=== Flagent Database Restoration ==="
echo "Backup file: $BACKUP_FILE"
echo "Database: $DB_NAME"
echo ""
read -p "This will DROP and recreate the database. Continue? (yes/no): " CONFIRM

if [ "$CONFIRM" != "yes" ]; then
  echo "Aborted"
  exit 0
fi

# Stop application
echo "[$(date -Iseconds)] Stopping Flagent..."
docker-compose stop flagent || echo "Flagent not running"

# Drop existing database
echo "[$(date -Iseconds)] Dropping existing database..."
PGPASSWORD="$DB_PASSWORD" psql \
  -h "$DB_HOST" \
  -U "$DB_USER" \
  -c "DROP DATABASE IF EXISTS $DB_NAME"

# Create new database
echo "[$(date -Iseconds)] Creating new database..."
PGPASSWORD="$DB_PASSWORD" psql \
  -h "$DB_HOST" \
  -U "$DB_USER" \
  -c "CREATE DATABASE $DB_NAME"

# Restore backup
echo "[$(date -Iseconds)] Restoring backup..."
gunzip -c "$BACKUP_FILE" | PGPASSWORD="$DB_PASSWORD" pg_restore \
  -h "$DB_HOST" \
  -U "$DB_USER" \
  -d "$DB_NAME" \
  --no-owner \
  --no-acl

# Verify restoration
echo "[$(date -Iseconds)] Verifying restoration..."
PGPASSWORD="$DB_PASSWORD" psql \
  -h "$DB_HOST" \
  -U "$DB_USER" \
  -d "$DB_NAME" \
  -c "SELECT 'flags', COUNT(*) FROM flags UNION ALL SELECT 'segments', COUNT(*) FROM segments"

# Start application
echo "[$(date -Iseconds)] Starting Flagent..."
docker-compose start flagent

echo "[$(date -Iseconds)] Database restoration completed successfully"
