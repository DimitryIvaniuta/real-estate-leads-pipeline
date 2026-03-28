package com.github.dimitryivaniuta.gateway.realestate.domain;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Immutable audit history entry for a lead change.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@Table("lead_activities")
public class LeadActivity {

    /** Unique activity identifier. */
    @Id
    private UUID id;

    /** Related lead identifier. */
    @Column("lead_id")
    private UUID leadId;

    /** Related tenant identifier. */
    @Column("tenant_id")
    private UUID tenantId;

    /** Activity type. */
    @Column("action_type")
    private LeadActionType actionType;

    /** Previous phase when applicable. */
    @Column("from_phase")
    private String fromPhase;

    /** New phase when applicable. */
    @Column("to_phase")
    private String toPhase;

    /** Who performed the change. */
    @Column("actor")
    private String actor;

    /** Timestamp of the activity. */
    @Column("occurred_at")
    private Instant occurredAt;

    /** Optional details. */
    @Column("details")
    private String details;
}
