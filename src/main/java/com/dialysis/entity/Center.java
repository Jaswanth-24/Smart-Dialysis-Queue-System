package com.dialysis.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "centers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Center {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String phone;

    @Column(name = "machine_count", nullable = false)
    private Integer machineCount;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @OneToMany(mappedBy = "center", fetch = FetchType.LAZY)
    private List<Machine> machines;

    @OneToMany(mappedBy = "center", fetch = FetchType.LAZY)
    private List<Patient> patients;
}
