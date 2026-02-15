-- Add optional client identifier for "who uses this flag" analytics.
-- Clients can send X-Client-Id header with evaluation requests.
ALTER TABLE evaluation_events ADD COLUMN IF NOT EXISTS client_id VARCHAR(255) NULL;
CREATE INDEX IF NOT EXISTS idx_eval_events_client_id ON evaluation_events (client_id) WHERE client_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_eval_events_flag_client_ts ON evaluation_events (flag_id, client_id, timestamp_ms) WHERE client_id IS NOT NULL;
