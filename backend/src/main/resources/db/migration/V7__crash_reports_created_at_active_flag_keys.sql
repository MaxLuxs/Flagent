-- Add columns used by CrashReportRepository (Exposed schema already has them; SQLite uses SchemaUtils).
ALTER TABLE crash_reports ADD COLUMN IF NOT EXISTS active_flag_keys TEXT;
ALTER TABLE crash_reports ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;
-- Backfill created_at from timestamp (ms) where null
UPDATE crash_reports SET created_at = to_timestamp("timestamp" / 1000.0) AT TIME ZONE 'UTC' WHERE created_at IS NULL;
ALTER TABLE crash_reports ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;
-- Allow NULL for existing rows that couldn't be backfilled (e.g. empty table); app always sends created_at on insert
