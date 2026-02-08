-- Flagent core schema (PostgreSQL)
-- Matches Exposed Tables.kt definitions

CREATE TABLE IF NOT EXISTS flags (
    id SERIAL PRIMARY KEY,
    "key" VARCHAR(64) NOT NULL,
    description TEXT NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT false,
    snapshot_id INTEGER NOT NULL DEFAULT 0,
    notes TEXT,
    data_records_enabled BOOLEAN NOT NULL DEFAULT false,
    entity_type VARCHAR(255),
    environment_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_flag_key ON flags ("key");
CREATE INDEX IF NOT EXISTS idx_flag_environment ON flags (environment_id);

CREATE TABLE IF NOT EXISTS segments (
    id SERIAL PRIMARY KEY,
    flag_id INTEGER NOT NULL REFERENCES flags(id) ON DELETE CASCADE,
    description TEXT,
    "rank" INTEGER NOT NULL DEFAULT 999,
    rollout_percent INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_segment_flagid ON segments (flag_id);

CREATE TABLE IF NOT EXISTS variants (
    id SERIAL PRIMARY KEY,
    flag_id INTEGER NOT NULL REFERENCES flags(id) ON DELETE CASCADE,
    "key" VARCHAR(255),
    attachment TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_variant_flagid ON variants (flag_id);

CREATE TABLE IF NOT EXISTS constraints (
    id SERIAL PRIMARY KEY,
    segment_id INTEGER NOT NULL REFERENCES segments(id) ON DELETE CASCADE,
    property VARCHAR(255) NOT NULL,
    operator VARCHAR(50) NOT NULL,
    value TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_constraint_segmentid ON constraints (segment_id);

CREATE TABLE IF NOT EXISTS distributions (
    id SERIAL PRIMARY KEY,
    segment_id INTEGER NOT NULL REFERENCES segments(id) ON DELETE CASCADE,
    variant_id INTEGER NOT NULL REFERENCES variants(id) ON DELETE CASCADE,
    variant_key VARCHAR(255),
    percent INTEGER NOT NULL DEFAULT 0,
    bitmap TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_distribution_segmentid ON distributions (segment_id);
CREATE INDEX IF NOT EXISTS idx_distribution_variantid ON distributions (variant_id);

CREATE TABLE IF NOT EXISTS tags (
    id SERIAL PRIMARY KEY,
    value VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_tag_value ON tags (value);

CREATE TABLE IF NOT EXISTS flags_tags (
    id SERIAL PRIMARY KEY,
    flag_id INTEGER NOT NULL REFERENCES flags(id) ON DELETE CASCADE,
    tag_id INTEGER NOT NULL REFERENCES tags(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX IF NOT EXISTS flags_tags_unique ON flags_tags (flag_id, tag_id);

CREATE TABLE IF NOT EXISTS flag_snapshots (
    id SERIAL PRIMARY KEY,
    flag_id INTEGER NOT NULL REFERENCES flags(id) ON DELETE CASCADE,
    updated_by VARCHAR(255),
    flag TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_flagsnapshot_flagid ON flag_snapshots (flag_id);

CREATE TABLE IF NOT EXISTS flag_entity_types (
    id SERIAL PRIMARY KEY,
    "key" VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS flag_entity_type_key ON flag_entity_types ("key");

CREATE TABLE IF NOT EXISTS webhooks (
    id SERIAL PRIMARY KEY,
    url TEXT NOT NULL,
    events TEXT NOT NULL,
    secret TEXT,
    enabled BOOLEAN NOT NULL DEFAULT true,
    tenant_id VARCHAR(255),
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_webhook_tenant ON webhooks (tenant_id);

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS evaluation_events (
    id BIGSERIAL PRIMARY KEY,
    flag_id INTEGER NOT NULL,
    timestamp_ms BIGINT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_eval_events_flag ON evaluation_events (flag_id);
CREATE INDEX IF NOT EXISTS idx_eval_events_timestamp ON evaluation_events (timestamp_ms);
CREATE INDEX IF NOT EXISTS idx_eval_events_flag_timestamp ON evaluation_events (flag_id, timestamp_ms);

CREATE TABLE IF NOT EXISTS analytics_events (
    id BIGSERIAL PRIMARY KEY,
    event_name VARCHAR(128) NOT NULL,
    event_params TEXT,
    user_id VARCHAR(255),
    session_id VARCHAR(255),
    platform VARCHAR(32),
    app_version VARCHAR(64),
    timestamp_ms BIGINT NOT NULL,
    tenant_id VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_analytics_event_name ON analytics_events (event_name);
CREATE INDEX IF NOT EXISTS idx_analytics_user ON analytics_events (user_id);
CREATE INDEX IF NOT EXISTS idx_analytics_session ON analytics_events (session_id);
CREATE INDEX IF NOT EXISTS idx_analytics_timestamp ON analytics_events (timestamp_ms);
CREATE INDEX IF NOT EXISTS idx_analytics_tenant ON analytics_events (tenant_id);
CREATE INDEX IF NOT EXISTS idx_analytics_event_timestamp ON analytics_events (event_name, timestamp_ms);

CREATE TABLE IF NOT EXISTS crash_reports (
    id BIGSERIAL PRIMARY KEY,
    stack_trace TEXT NOT NULL,
    message TEXT NOT NULL,
    platform VARCHAR(64) NOT NULL,
    app_version VARCHAR(64),
    device_info TEXT,
    breadcrumbs TEXT,
    custom_keys TEXT,
    "timestamp" BIGINT NOT NULL,
    tenant_id VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_crash_timestamp ON crash_reports ("timestamp");
CREATE INDEX IF NOT EXISTS idx_crash_tenant ON crash_reports (tenant_id);
CREATE INDEX IF NOT EXISTS idx_crash_platform_timestamp ON crash_reports (platform, "timestamp");
