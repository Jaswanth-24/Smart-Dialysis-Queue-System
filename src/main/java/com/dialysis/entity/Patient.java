package com.dialysis.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ── Auth/User Mapping ─────────────────────────────────────
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // ── Center Mapping ────────────────────────────────────────
    // kept separately for easier querying/filtering
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id", nullable = false)
    private Center center;

    // Assigned doctor (User with DOCTOR role)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private User doctor;

    // ── Personal / Medical Info ───────────────────────────────
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "blood_group", length = 5)
    private String bloodGroup;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;

    // ── Tracking ──────────────────────────────────────────────
    @Column(name = "total_sessions", nullable = false)
    @Builder.Default
    private Integer totalSessions = 0;

    @Column(name = "last_consult_trigger_session", nullable = false)
    @Builder.Default
    private Integer lastConsultTriggerSession = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // ── Audit ─────────────────────────────────────────────────
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Relationships ─────────────────────────────────────────
    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    private List<DialysisSession> sessions;

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    private List<DoctorConsultation> consultations;
}