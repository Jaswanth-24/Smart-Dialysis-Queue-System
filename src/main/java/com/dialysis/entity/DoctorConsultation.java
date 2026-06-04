package com.dialysis.entity;

import com.dialysis.enums.ConsultationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "doctor_consultations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DoctorConsultation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private User doctor;

    @Column(name = "consult_date")
    private LocalDate consultDate;

    /** The session number that triggered this consult (e.g. 10, 20, 30) */
    @Column(name = "trigger_session")
    private Integer triggerSession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ConsultationStatus status = ConsultationStatus.PENDING;

    @Column(name = "doctor_notes", columnDefinition = "TEXT")
    private String doctorNotes;

    @Column(columnDefinition = "TEXT")
    private String prescription;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Reports attached to this consult ──────────────────────
    @OneToMany(mappedBy = "consultation", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TestReport> reports;
}
