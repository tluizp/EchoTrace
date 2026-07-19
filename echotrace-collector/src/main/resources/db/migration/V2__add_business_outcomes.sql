ALTER TABLE events ADD COLUMN IF NOT EXISTS outcome_name VARCHAR(255);
ALTER TABLE events ADD COLUMN IF NOT EXISTS journey_id VARCHAR(255);
ALTER TABLE events ADD COLUMN IF NOT EXISTS journey_type VARCHAR(255);
ALTER TABLE events ADD COLUMN IF NOT EXISTS journey_stage VARCHAR(255);
ALTER TABLE events ADD COLUMN IF NOT EXISTS outcome_reason VARCHAR(255);
ALTER TABLE events ADD COLUMN IF NOT EXISTS business_value NUMERIC(19, 4);
ALTER TABLE events ADD COLUMN IF NOT EXISTS currency VARCHAR(16);
ALTER TABLE events ADD COLUMN IF NOT EXISTS service_version VARCHAR(255);
ALTER TABLE events ADD COLUMN IF NOT EXISTS deployment_id VARCHAR(255);
ALTER TABLE events ADD COLUMN IF NOT EXISTS commit_sha VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_events_outcome_created_at
    ON events (outcome_name, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_events_journey
    ON events (journey_type, journey_id, created_at);
CREATE INDEX IF NOT EXISTS idx_events_deployment
    ON events (service_name, deployment_id, created_at DESC);
