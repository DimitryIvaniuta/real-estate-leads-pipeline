package com.github.dimitryivaniuta.gateway.realestate.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Reactive aggregate root describing an agent-client lead.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@Table("leads")
public class Lead {

    /** Unique lead identifier. */
    @Id
    private UUID id;

    /** Tenant identifier for isolation. */
    @Column("tenant_id")
    private UUID tenantId;

    /** Assigned agent login or id. */
    @Column("agent_id")
    private String agentId;

    /** Client full name. */
    @Column("client_name")
    private String clientName;

    /** Client email. */
    @Column("client_email")
    private String clientEmail;

    /** Client phone. */
    @Column("client_phone")
    private String clientPhone;

    /** Property area or city. */
    @Column("property_area")
    private String propertyArea;

    /** Budget or estimated deal value. */
    @Column("budget_amount")
    private BigDecimal budgetAmount;

    /** Current pipeline phase. */
    @Column("phase")
    private LeadPhase phase;

    /** Free-form source label. */
    @Column("source_channel")
    private String sourceChannel;

    /** Creation timestamp. */
    @Column("created_at")
    private Instant createdAt;

    /** Update timestamp. */
    @Column("updated_at")
    private Instant updatedAt;

    /** Last actor that changed state. */
    @Column("last_modified_by")
    private String lastModifiedBy;
}
