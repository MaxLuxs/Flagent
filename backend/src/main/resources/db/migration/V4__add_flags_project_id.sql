-- Optional project scope for flags (Enterprise: filter by project).
-- No FK in core; enterprise may add FK to projects when present.
ALTER TABLE flags ADD COLUMN IF NOT EXISTS project_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_flag_project ON flags (project_id);
