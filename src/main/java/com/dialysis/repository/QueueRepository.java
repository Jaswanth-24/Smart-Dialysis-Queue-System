package com.dialysis.repository;

import com.dialysis.entity.QueueEntry;
import com.dialysis.enums.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QueueRepository extends JpaRepository<QueueEntry, UUID> {

    boolean existsByPatientIdAndQueueDateAndStatusNot(
            UUID patientId,
            LocalDate queueDate,
            QueueStatus status
    );

    Optional<QueueEntry>
    findTopByCenterIdAndQueueDateOrderByTokenNumberDesc(
            UUID centerId,
            LocalDate queueDate
    );
    List<QueueEntry> findByCenterIdAndQueueDateAndStatusIn(
            UUID centerId,
            LocalDate queueDate,
            List<QueueStatus> statuses
    );
    Optional<QueueEntry>
    findByPatientIdAndQueueDate(
            UUID patientId,
            LocalDate queueDate
    );
    List<QueueEntry>
    findByCenterIdAndStatusOrderByTokenNumberAsc(
            UUID centerId,
            QueueStatus status
    );
}