package com.dialysis.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "session_values")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SessionValues {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private DialysisSession session;

    // ── Pre-session ───────────────────────────────────────────
    @Column(name = "pre_bp", length = 20)
    private String preBp;           // e.g. "140/90"

    @Column(name = "pre_weight", precision = 5, scale = 2)
    private BigDecimal preWeight;   // kg

    @Column(name = "pre_complaints", columnDefinition = "TEXT")
    private String preComplaints;

    // ── Mid-session ───────────────────────────────────────────
    @Column(name = "uf_rate", precision = 5, scale = 2)
    private BigDecimal ufRate;      // mL/hour

    @Column(name = "blood_flow_rate", precision = 5, scale = 2)
    private BigDecimal bloodFlowRate; // mL/min

    @Column(name = "dialysate_flow_rate", precision = 6, scale = 2)
    private BigDecimal dialysateFlowRate;

    // ── Post-session ──────────────────────────────────────────
    @Column(name = "post_bp", length = 20)
    private String postBp;

    @Column(name = "post_weight", precision = 5, scale = 2)
    private BigDecimal postWeight;  // kg

    @Column(name = "uf_removed", precision = 5, scale = 2)
    private BigDecimal ufRemoved;   // litres actually removed

    @Column(name = "complications", columnDefinition = "TEXT")
    private String complications;

    /** Flag set automatically when BP or weight is outside safe range */
    @Column(name = "has_abnormal_values", nullable = false)
    @Builder.Default
    private Boolean hasAbnormalValues = false;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
