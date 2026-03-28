CREATE TABLE IF NOT EXISTS leads (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    agent_id VARCHAR(120) NOT NULL,
    client_name VARCHAR(255) NOT NULL,
    client_email VARCHAR(255),
    client_phone VARCHAR(64) NOT NULL,
    property_area VARCHAR(255) NOT NULL,
    budget_amount NUMERIC(19, 2) NOT NULL,
    phase VARCHAR(32) NOT NULL,
    source_channel VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    last_modified_by VARCHAR(120) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_leads_tenant_id ON leads (tenant_id);
CREATE INDEX IF NOT EXISTS idx_leads_agent_id ON leads (agent_id);
CREATE INDEX IF NOT EXISTS idx_leads_phase ON leads (phase);
