package com.dialysis.service;

import com.dialysis.dto.request.QueueCheckInRequest;
import com.dialysis.dto.response.LiveQueueResponse;
import com.dialysis.dto.response.QueueCheckInResponse;
import com.dialysis.entity.Patient;
import com.dialysis.entity.QueueEntry;
import com.dialysis.enums.MachineStatus;
import com.dialysis.enums.QueueStatus;
import com.dialysis.exception.BadRequestException;
import com.dialysis.exception.ResourceNotFoundException;
import com.dialysis.repository.MachineRepository;
import com.dialysis.repository.PatientRepository;
import com.dialysis.repository.QueueRepository;
import com.dialysis.websocket.QueueBroadcaster;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueRepository queueRepository;
    private final PatientRepository patientRepository;
    private final MachineRepository machineRepository;
    private final SessionService sessionService;
    private final QueueBroadcaster queueBroadcaster;

    @Transactional
    public QueueCheckInResponse checkIn(
            QueueCheckInRequest request
    ){

        Patient patient = patientRepository.findById(
                request.patientId()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Patient not found"
                )
        );

        LocalDate today = LocalDate.now();

        // Prevent duplicate same-day queue
        boolean activeEntryExists =
                queueRepository.existsByPatientIdAndQueueDateAndStatusNot(
                        patient.getId(),
                        today,
                        QueueStatus.CANCELLED
                );

        if(activeEntryExists){
            throw new BadRequestException(
                    "Patient already checked-in today"
            );
        }

        // Generate next token
        Integer nextToken =
                queueRepository
                        .findTopByCenterIdAndQueueDateOrderByTokenNumberDesc(
                                patient.getCenter().getId(),
                                today
                        )
                        .map(q -> q.getTokenNumber() + 1)
                        .orElse(1);

        QueueEntry entry = QueueEntry.builder()
                .patient(patient)
                .center(patient.getCenter())
                .tokenNumber(nextToken)
                .queueDate(today)
                .checkedInAt(LocalDateTime.now())
                .status(QueueStatus.WAITING)
                .build();


        QueueEntry saved = queueRepository.save(entry);

        // Auto-assign a machine if one is available right now.
        // If none is free the patient stays WAITING and MachineSchedulerService
        // will assign as soon as a machine finishes its cleaning cycle.
        boolean machineAvailable =
                machineRepository
                        .findFirstByCenterIdAndStatusAndIsActiveTrue(
                                patient.getCenter().getId(),
                                MachineStatus.AVAILABLE
                        )
                        .isPresent();

        if (machineAvailable) {
            sessionService.assignMachine(saved.getId());
            saved = queueRepository.findById(saved.getId()).orElse(saved);
        }

        QueueCheckInResponse response = QueueCheckInResponse.builder()
                .queueId(saved.getId())
                .tokenNumber(saved.getTokenNumber())
                .priorityScore(saved.getPriorityScore())
                .status(saved.getStatus())
                .patientName(
                        saved.getPatient()
                                .getUser()
                                .getName()
                )
                .message(
                        saved.getStatus() == QueueStatus.ASSIGNED
                                ? "Patient checked-in and machine assigned — waiting for technician"
                                : "Patient checked-in successfully — waiting for a machine"
                )
                .build();

        // Bug 2 fix: push updated queue to all connected clients
        queueBroadcaster.broadcastQueueUpdate(
                patient.getCenter().getId(),
                getLiveQueue(patient.getCenter().getId())
        );

        return response;
    }

    public List<QueueEntry> getTodayQueue(UUID centerId){

        List<QueueEntry> entries =
                queueRepository
                        .findByCenterIdAndQueueDateAndStatusIn(
                                centerId,
                                LocalDate.now(),
                                List.of(
                                        QueueStatus.WAITING,
                                        QueueStatus.ASSIGNED,
                                        QueueStatus.IN_SESSION
                                )
                        );

        return entries.stream()
                .sorted((a, b) ->
                        a.getTokenNumber()
                                .compareTo(b.getTokenNumber()))
                .toList();
    }

    public List<LiveQueueResponse> getLiveQueue(
            UUID centerId
    ) {

        List<QueueEntry> entries =
                getTodayQueue(centerId);

        int averageDialysisMinutes = 240;

        // Use actual active machine count for this center (replaces hardcoded value)
        int machineCount = Math.max(
                machineRepository.countByCenterIdAndIsActiveTrue(centerId),
                1   // guard against division by zero if no machines are registered yet
        );

        int waitingIndex = 0;

        List<LiveQueueResponse> response =
                new ArrayList<>();

        for (QueueEntry entry : entries) {

            int estimatedTime = 0;

            if (entry.getStatus()
                    == QueueStatus.WAITING) {

                estimatedTime =
                        (waitingIndex * averageDialysisMinutes)
                                / machineCount;

                waitingIndex++;
            }

            response.add(
                    LiveQueueResponse.builder()
                            .tokenNumber(
                                    entry.getTokenNumber()
                            )
                            .patientName(
                                    entry.getPatient()
                                            .getUser()
                                            .getName()
                            )
                            .status(
                                    entry.getStatus()
                            )
                            .assignedMachine(
                                    entry.getMachine() != null
                                            ? entry.getMachine().getMachineNumber()
                                            : null
                            )
                            .estimatedWaitMinutes(
                                    estimatedTime
                            )
                            .build()
            );
        }

        return response;
    }
    public void cancelQueue(UUID queueId){

        QueueEntry entry =
                queueRepository.findById(queueId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Queue entry not found"
                                )
                        );

        if(entry.getStatus() == QueueStatus.COMPLETED){
            throw new BadRequestException(
                    "Completed queue cannot be cancelled"
            );
        }

        if(entry.getStatus() == QueueStatus.IN_SESSION){
            throw new BadRequestException(
                    "Cannot cancel a session that is in progress"
            );
        }

        // If a machine was reserved for this patient, release it back to AVAILABLE
        if (entry.getStatus() == QueueStatus.ASSIGNED && entry.getMachine() != null) {
            entry.getMachine().setStatus(MachineStatus.AVAILABLE);
            machineRepository.save(entry.getMachine());
        }

        entry.setStatus(QueueStatus.CANCELLED);
        queueRepository.save(entry);

        // Bug 2 fix: push updated queue to all connected clients
        queueBroadcaster.broadcastQueueUpdate(
                entry.getCenter().getId(),
                getLiveQueue(entry.getCenter().getId())
        );
    }
}