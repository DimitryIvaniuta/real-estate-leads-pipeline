CREATE TABLE IF NOT EXISTS lead_activities (
    id UUID PRIMARY KEY,
    lead_id UUID NOT NULL REFERENCES leads (id) ON DELETE CASCADE,
    tenant_id UUID NOT NULL,
    action_type VARCHAR(32) NOT NULL,
    from_phase VARCHAR(32),
    to_phase VARCHAR(32),
    actor VARCHAR(120) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    details TEXT
);

CREATE INDEX IF NOT EXISTS idx_lead_activities_lead_id_occurred_at
    ON lead_activities (lead_id, occurred_at DESC);

CREATE INDEX IF NOT EXISTS idx_lead_activities_tenant_id
    ON lead_activities (tenant_id);
