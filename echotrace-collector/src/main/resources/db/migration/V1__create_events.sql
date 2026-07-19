CREATE TABLE IF NOT EXISTS events (
    id UUID PRIMARY KEY,
    spec_version VARCHAR(32),
    event_id VARCHAR(255) UNIQUE,
    event_version INTEGER NOT NULL,
    event_name VARCHAR(255),
    service_name VARCHAR(255),
    environment VARCHAR(255),
    status VARCHAR(64),
    duration_ms BIGINT NOT NULL,
    trace_id VARCHAR(255),
    span_id VARCHAR(255),
    created_at TIMESTAMPTZ,
    observed_at TIMESTAMPTZ,
    payload JSONB
);

CREATE INDEX IF NOT EXISTS idx_events_name_created_at
    ON events (event_name, created_at DESC);
