package com.dialysis.entity;

import com.dialysis.enums.MachineStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "machines")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id", nullable = false)
    private Center center;

    @Column(name = "machine_number", nullable = false)
    private String machineNumber;   // e.g. "M-01", "M-02"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MachineStatus status = MachineStatus.AVAILABLE;

    @Column(name = "last_serviced")
    private LocalDate lastServiced;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "available_at")
    private LocalDateTime availableAt;
}
