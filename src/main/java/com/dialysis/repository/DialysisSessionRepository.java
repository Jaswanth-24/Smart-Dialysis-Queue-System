package com.dialysis.repository;

import com.dialysis.entity.DialysisSession;
import com.dialysis.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DialysisSessionRepository extends JpaRepository<DialysisSession, UUID> {

    Integer countByPatientId(UUID patientId);

    Optional<DialysisSession> findByPatientIdAndStatus(UUID patientId, SessionStatus status);
}
