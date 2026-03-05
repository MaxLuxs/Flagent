-- Link analytics and evaluation events to flags/variants for richer analytics.

-- Evaluation events: optional variant_id for per-variant evaluation counts.
ALTER TABLE evaluation_events ADD COLUMN IF NOT EXISTS variant_id INTEGER NULL;
CREATE INDEX IF NOT EXISTS idx_eval_events_variant ON evaluation_events (variant_id);
CREATE INDEX IF NOT EXISTS idx_eval_events_flag_variant_ts
    ON evaluation_events (flag_id, variant_id, timestamp_ms);

-- Analytics events: optional flag_id and variant_id to associate product events with experiments.
ALTER TABLE analytics_events ADD COLUMN IF NOT EXISTS flag_id INTEGER NULL;
ALTER TABLE analytics_events ADD COLUMN IF NOT EXISTS variant_id INTEGER NULL;
CREATE INDEX IF NOT EXISTS idx_analytics_flag ON analytics_events (flag_id);
CREATE INDEX IF NOT EXISTS idx_analytics_variant ON analytics_events (variant_id);
CREATE INDEX IF NOT EXISTS idx_analytics_flag_timestamp
    ON analytics_events (flag_id, timestamp_ms);

