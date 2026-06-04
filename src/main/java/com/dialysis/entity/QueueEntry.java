package com.dialysis.entity;

import com.dialysis.enums.QueueStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "queue_entries",
    uniqueConstraints = @UniqueConstraint(columnNames = {"patient_id", "queue_date"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QueueEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id", nullable = false)
    private Center center;

    @Column(name = "token_number", nullable = false)
    private Integer tokenNumber;


    @Column(name = "priority_score", nullable = false)
    @Builder.Default
    private Double priorityScore = 0.0;

    @Column(name = "is_emergency", nullable = false)
    @Builder.Default
    private Boolean isEmergency = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private QueueStatus status = QueueStatus.WAITING;

    @Column(name = "queue_date", nullable = false)
    private LocalDate queueDate;

    @Column(name = "checked_in_at", nullable = false)
    private LocalDateTime checkedInAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id")
    private Machine machine;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
