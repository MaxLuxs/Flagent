-- Feature dependencies: flag can depend on other flags by key
ALTER TABLE flags ADD COLUMN IF NOT EXISTS depends_on TEXT;
