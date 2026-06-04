package com.dialysis.entity;

import com.dialysis.enums.TestType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "test_reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TestReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id")
    private DoctorConsultation consultation;

    // Uploaded by staff (receptionist / doctor)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_type", nullable = false)
    private TestType testType;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;         // S3 / Cloudinary URL

    @Column(name = "file_name")
    private String fileName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
